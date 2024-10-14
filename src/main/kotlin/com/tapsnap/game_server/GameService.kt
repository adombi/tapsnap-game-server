package com.tapsnap.game_server

import io.cloudevents.CloudEvent
import io.cloudevents.core.v1.CloudEventBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URI
import java.util.UUID
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

private val logger = KotlinLogging.logger {}

@Service
class GameService(
    private var repository: MutableMap<String, Game> = mutableMapOf()
) {
    init {
        Flux.fromIterable(listOf("Creative_it"))
            .map { Game(it) }
            .flatMap { this.save(it).toFlux() }
            .subscribe()
    }

    fun get(id: String): Mono<Game> {
        return repository[id].toMono()
    }

    fun getAll(): Flux<Game> {
        return Flux.fromIterable(repository.values)
    }

    fun save(game: Game): Mono<Game> {
        if (repository[game.id] == null) {
            repository[game.id] = game
        }
        return Mono.just(game)
    }

    fun reset(gameId: String): Mono<Game> {
        val game = repository[gameId]
        if (game != null) {
            game.users.removeIf { true }
            game.results.clear()
        }
        return game.toMono()
    }

    fun restart(gameId: String): Mono<Game> {
        val game = repository[gameId]
        if (game != null) {
            val eventId = UUID.randomUUID().toString()
            game.results.forEach {
                result -> result.value.clear()
            }
            game.eventBus.emitNext(
                CloudEventBuilder()
                    .withId(eventId)
                    .withSource(URI.create("https://snaptap.adombi.dev"))
                    .withType("Restarted")
                    .build(), Sinks.EmitFailureHandler.FAIL_FAST)
            game.dashboardEventBus.emitNext(
                CloudEventBuilder()
                    .withId(eventId)
                    .withSource(URI.create("https://snaptap.adombi.dev"))
                    .withType("RefreshResults")
                    .build(), Sinks.EmitFailureHandler.FAIL_FAST)
        }
        return game.toMono()
    }

    fun react(gameId: String, react: React): Mono<Game> {
        val game = repository[gameId]
        if (game != null) {
            val reactions = game.results[react.playerName]
            if (reactions == null) {
                game.results[react.playerName] = mutableListOf(react.respondTimeMillis)
            } else {
                reactions.add(react.respondTimeMillis)
            }
        }
        return game.toMono()
    }

    fun results(gameId: String): Mono<Map<String, List<Int>>> {
        return repository[gameId]?.results.toMono()
    }

    fun addUserToGame(gameId: String, user: String): Mono<Game> {
        val game = repository[gameId]
            ?: return Mono.error(RuntimeException("No such game of \"$gameId\""))
        if (!game.users.contains(user)) {
            game.users.add(user)
            game.results[user] = mutableListOf()
        }
        logger.debug { "GAME: $game" }
        return Mono.just(game)
    }

    fun eventBus(gameId: String): Sinks.Many<CloudEvent> {
        return repository[gameId]?.eventBus ?: throw RuntimeException("No Game is registered for game ID: $gameId")
    }

    fun dashboardEventBus(gameId: String): Sinks.Many<CloudEvent> {
        return repository[gameId]?.dashboardEventBus ?: throw RuntimeException("No Game is registered for game ID: $gameId")
    }
}