package com.creative_it.meetup_game_server

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.CloudEventUtils.mapData
import io.cloudevents.core.v1.CloudEventBuilder
import io.cloudevents.jackson.PojoCloudEventDataMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URI
import java.util.UUID
import lombok.extern.slf4j.Slf4j
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

private val logger = KotlinLogging.logger {}

@Slf4j
@Controller
class SensorGamingServer(
    val objectMapper: ObjectMapper
) {

    @MessageMapping("sensor-gaming/{gameId}")
    fun feedMarketData(@DestinationVariable gameId: String, rMessage: Flux<CloudEvent>): Flux<CloudEvent>  {
        return rMessage
            .mapNotNull { e -> mapData(e, PojoCloudEventDataMapper.from(
                objectMapper,
                objectMapper.typeFactory.findClass("com.creative_it.meetup_game_server.User")
            ))?.value as User}
            .filter {s -> s.name == "Start"}
//            .withLatestFrom<Long, EventWithServerConfig>(Flux.interval(1.seconds.toJavaDuration()))
//            { a: CloudEvent, b: Long -> EventWithServerConfig(a, b) }
            .log()
//            .takeUntil {event -> event.tick == 2L}
            .map {user -> CloudEventBuilder()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create("http://snaptap.adombi.dev"))
                .withType("wtf")
                .withSubject("$gameId - ${user.name}")
                .build() }
    }
    data class EventWithServerConfig(val event: CloudEvent, val tick: Long)
    data class SettingsWithInterval(val settings: Settings, val tick: Long)
    data class Settings(val name: String, val start: Boolean)
}