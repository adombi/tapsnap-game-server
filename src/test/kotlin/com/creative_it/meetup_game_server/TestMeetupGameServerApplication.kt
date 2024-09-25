package com.creative_it.meetup_game_server

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<MeetupGameServerApplication>().with(TestcontainersConfiguration::class).run(*args)
}
