package com.creative_it.meetup_game_server

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.CloudEventUtils.mapData
import io.cloudevents.core.v1.CloudEventBuilder
import io.cloudevents.jackson.PojoCloudEventDataMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers

private val logger = KotlinLogging.logger {}

@Controller
class SensorGamingServer(
    val objectMapper: ObjectMapper,
    val gameService: GameService
) {

    @MessageMapping("sensor-gaming/{gameId}")
    fun game(@DestinationVariable gameId: String, rMessage: Flux<CloudEvent>): Flux<CloudEvent>  {
        return rMessage
            .flatMap map@{ e ->
                when (e.type) {
                    "com.creative_it.meetup_game_server.JoinRequest" -> {
                        return@map Flux.just(mapToType<JoinRequest>(e))
                            .map<User> { joinRequest -> User(joinRequest.playerName) }
                            .flatMap<User> { user -> gameService.addUserToGame(gameId, user).map{ g -> user } }
                            .map<CloudEvent> { user ->
                                CloudEventBuilder(e)
                                    .withType("Joined")
                                    .withSubject(objectMapper.writeValueAsString(user))
                                    .build()
                            }
                    }
                    "com.creative_it.meetup_game_server.StartGame" -> {
                        return@map Flux.interval(1.seconds.toJavaDuration(), Schedulers.single())
                            .take(3)
                            .map<CloudEvent> { tick ->
                                CloudEventBuilder(e)
                                    .withType("CountDown")
                                    .withSubject(objectMapper.writeValueAsString(tick))
                                    .build()
                            }
                    }
                }
                return@map Flux.just(CloudEventBuilder(e)
                    .withType("wtf")
                    .withSubject("DEFAULT")
                    .build())
            }
            .flatMap<CloudEvent> { e ->
                val eventBus = gameService.eventBus(gameId)
                eventBus.emitNext(e, Sinks.EmitFailureHandler.FAIL_FAST)
                eventBus.asFlux()
            }
            .log()
    }

    fun <T> mapToType(e: CloudEvent): T {
        return mapData(e, PojoCloudEventDataMapper.from(
            objectMapper,
            objectMapper.typeFactory.findClass(e.type)
        ))?.value as T
    }
}