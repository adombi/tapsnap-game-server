package com.tapsnap.game_server

import io.cloudevents.CloudEvent
import io.github.oshai.kotlinlogging.KotlinLogging
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
        return repository[id]?.let { Mono.just(it) } ?: Mono.empty()
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
            return Mono.just(game)
        } else {
            return Mono.empty()
        }
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
        return game?.toMono() ?: Mono.empty()
    }

    fun results(gameId: String): Mono<Map<String, List<Int>>> {
        return repository[gameId]?.results.toMono()
    }

    fun addUserToGame(gameId: String, user: String): Mono<Game> {
        val game = repository[gameId]
            ?: return Mono.error(RuntimeException("No such game of \"$gameId\""))
        if (!game.users.contains(user)) {
            game.users.add(user)
        }
        logger.info { "GAME: $game" }
        return Mono.just(game)
    }

    fun eventBus(gameId: String): Sinks.Many<CloudEvent> {
        return repository[gameId]?.eventBus ?: throw RuntimeException("No Game is registered for game ID: $gameId")
    }
}