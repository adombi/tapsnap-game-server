package com.tapsnap.game_server

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.CloudEventUtils.mapData
import io.cloudevents.core.v1.CloudEventBuilder
import io.cloudevents.jackson.PojoCloudEventDataMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

private val logger = KotlinLogging.logger {}

@Controller
class TapSnapGamingServer(
    val objectMapper: ObjectMapper,
    val gameService: GameService,
) {

    @MessageMapping("tap-snap/{gameId}")
    fun game(@DestinationVariable gameId: String, rMessage: Flux<CloudEvent>): Flux<CloudEvent>  {
        return rMessage
            .flatMap map@{ e ->
                when (e.type) {
                    "com.tapsnap.game_server.Connect" -> {
                        return@map Flux.just<CloudEvent>(
                            CloudEventBuilder(e)
                                .withType("Connected")
                                .build()
                        )
                    }
                    "com.tapsnap.game_server.JoinRequest" -> {
                        return@map Flux.just(mapToType<JoinRequest>(e))
                            .map<User> { joinRequest -> User(joinRequest.playerName) }
                            .flatMap<Game> { user -> gameService.addUserToGame(gameId, user) }
                            .map<CloudEvent> { game ->
                                CloudEventBuilder(e)
                                    .withType("Joined")
                                    .withData(objectMapper.writeValueAsBytes(game))
                                    .build()
                            }
                    }
                    "com.tapsnap.game_server.StartGame" -> {
                        return@map Flux.concat(
                            countDownFrom3()
                                .map<CloudEvent> { tick ->
                                    CloudEventBuilder(e)
                                        .withType("CountDown")
                                        .withData(objectMapper.writeValueAsBytes(tick))
                                        .build()
                            },
                            Flux.concat(randomInterval(1337), randomInterval(8008), randomInterval(987654))
                                .map<CloudEvent> { tick ->
                                    CloudEventBuilder(e)
                                        .withType("InProgress")
                                        .withData(objectMapper.writeValueAsBytes(tick))
                                        .build()
                            }
                        )
                    }
                    "com.tapsnap.game_server.React" -> {
                        return@map Flux.just(mapToType<React>(e))
                            .flatMap { react -> gameService.react(gameId, react) }
                            .map { CloudEventBuilder(e).build() }
                            .filter { false }
                    }
                }
                return@map Flux.just(CloudEventBuilder(e)
                    .withType("wtf")
                    .withSubject("DEFAULT")
                    .build())
            }
            .flatMap { e ->
                val eventBus = gameService.eventBus(gameId)
                eventBus.emitNext(e, Sinks.EmitFailureHandler.FAIL_FAST)
                eventBus.asFlux()
            }
            .log()
    }

    private fun countDownFrom3() = listOf(3, 2, 1).toFlux().delayElements(1.seconds.toJavaDuration()).take(3)

    private fun randomInterval(data: Int) = data.toMono().delayElement(Random.nextInt(1000, 5001).milliseconds.toJavaDuration())

    fun <T> mapToType(e: CloudEvent): T {
        return mapData(e, PojoCloudEventDataMapper.from(
            objectMapper,
            objectMapper.typeFactory.findClass(e.type)
        ))?.value as T
    }
}