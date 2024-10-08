package com.tapsnap.game_server

import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/games")
@CrossOrigin(origins = ["http://localhost:3000"])
@RequiredArgsConstructor
class GameController(
    val gameService: GameService
) {
    @GetMapping("/{id}")
    fun get(@PathVariable id: String): Mono<Game> {
        return gameService.get(id)
    }
    @GetMapping
    fun getAll(): Flux<Game> {
        return gameService.getAll()
    }

    @PostMapping
    fun save(@RequestBody game: Game): Mono<Game> {
        return gameService.save(game)
    }

    @GetMapping("/{id}/reset")
    fun reset(@PathVariable id: String): Mono<Game> {
        return gameService.reset(id)
    }

    @GetMapping("/{id}/results")
    fun results(@PathVariable id: String): Mono<Map<User, List<Int>>> {
        return gameService.results(id)
    }

    @PostMapping("/{gameId}")
    fun addUserToGame(@PathVariable gameId: String, @RequestBody user: User): Mono<Game> {
        return gameService.addUserToGame(gameId, user)
    }
}