package com.creative_it.meetup_game_server

import lombok.RequiredArgsConstructor
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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

    @PostMapping("/{gameId}")
    fun addUserToGame(@PathVariable gameId: String, @RequestBody user: User): Mono<Game> {
        return gameService.addUserToGame(gameId, user)
    }
}