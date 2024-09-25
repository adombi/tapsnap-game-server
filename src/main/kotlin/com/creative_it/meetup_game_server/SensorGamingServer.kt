package com.creative_it.meetup_game_server

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import lombok.extern.slf4j.Slf4j
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

private val logger = KotlinLogging.logger {}

@Slf4j
@Controller
class SensorGamingServer {

    @MessageMapping("sensor-gaming/{gameId}")
    fun feedMarketData(@DestinationVariable gameId: String, settings: Flux<Settings>): Flux<String>  {
        val interval = Flux.interval(1.seconds.toJavaDuration())

        return Flux.combineLatest<Settings, Long, SettingsWithInterval>(settings, interval)
            { a: Settings, b: Long -> SettingsWithInterval(a, b) }
            .log()
            .takeUntil {settingsAndTick -> settingsAndTick.tick == settingsAndTick.settings.takeUntil}
            .map {settingsAndTick -> "$gameId - ${settingsAndTick.settings.name} - ${settingsAndTick.tick}"}
    }

    data class SettingsWithInterval(val settings: Settings, val tick: Long)
    data class Settings(val name: String, val takeUntil: Long)
}