package com.creative_it.meetup_game_server

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

private val logger = KotlinLogging.logger {}

@Service
class GameService(
    var repository: MutableMap<String, Game> = mutableMapOf<String, Game>()
) {
    init {
        Flux.fromIterable<String>(listOf<String>("Creative_IT", "asdf"))
            .map<Game> { Game(it) }
            .flatMap<Game> { this.save(it).toFlux() }
            .subscribe()
    }

    fun get(id: String): Mono<Game> {
        return repository[id]?.let { Mono.just(it) } ?: Mono.empty()
    }

    fun getAll(): Flux<Game> {
        return Flux.fromIterable<Game>(repository.values)
    }

    fun save(game: Game): Mono<Game> {
        if (repository[game.id] == null) {
            repository[game.id] = game
        }
        return Mono.just(game)
    }

    fun addUserToGame(gameId: String, user: User): Mono<Game> {
        val game = repository[gameId]
        if (game == null) {
            return Mono.error<Game>(RuntimeException("WTF?!"))
        }
        game.users.add(user)
        logger.info { "GAME: $game" }
        return Mono.just(game)
    }
}