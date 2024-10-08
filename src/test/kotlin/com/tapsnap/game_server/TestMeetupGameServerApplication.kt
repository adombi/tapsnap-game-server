package com.tapsnap.game_server

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<TapsnapGameServerApplication>().with(TestcontainersConfiguration::class).run(*args)
}
