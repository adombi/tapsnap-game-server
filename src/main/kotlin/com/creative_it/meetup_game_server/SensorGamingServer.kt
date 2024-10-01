package com.creative_it.meetup_game_server

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.CloudEventUtils.mapData
import io.cloudevents.core.v1.CloudEventBuilder
import io.cloudevents.jackson.PojoCloudEventDataMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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
                        return@map Mono.just(mapToType<JoinRequest>(e))
                            .map<User> { joinRequest -> User(joinRequest.playerName) }
                            .flatMap<User> { user -> gameService.addUserToGame(gameId, user).map{ g -> user } }
                            .map<CloudEvent> { user ->
                                CloudEventBuilder(e)
                                    .withType("Joined")
                                    .withSubject(objectMapper.writeValueAsString(user))
                                    .build()
                            }
                    }
                }
                return@map Mono.just(CloudEventBuilder(e)
                    .withType("wtf")
                    .withSubject("DEFAULT")
                    .build())
            }
//            .filter {s -> s.name == "Start"}
//            .withLatestFrom<Long, EventWithServerConfig>(Flux.interval(1.seconds.toJavaDuration()))
//            { a: CloudEvent, b: Long -> EventWithServerConfig(a, b) }
            .log()
//            .takeUntil {event -> event.tick == 2L}
//            .map {user -> CloudEventBuilder()
//                .withId(UUID.randomUUID().toString())
//                .withSource(URI.create("https://snaptap.adombi.dev"))
//                .withType("wtf")
//                .withSubject("$gameId $user.name")
//                .build() }
    }

    fun <T> mapToType(e: CloudEvent): T {
        return mapData(e, PojoCloudEventDataMapper.from(
            objectMapper,
            objectMapper.typeFactory.findClass(e.type)
        ))?.value as T
    }
}