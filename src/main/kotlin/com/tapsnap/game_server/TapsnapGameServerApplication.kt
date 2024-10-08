package com.tapsnap.game_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TapsnapGameServerApplication

fun main(args: Array<String>) {
	runApplication<TapsnapGameServerApplication>(*args)
}
