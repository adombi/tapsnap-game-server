package com.creative_it.meetup_game_server

import io.cloudevents.CloudEvent
import reactor.core.publisher.Sinks

data class Game(val id: String, val users: MutableList<User> = mutableListOf(), val eventBus: Sinks.Many<CloudEvent> = Sinks.unsafe().many().multicast().directBestEffort<CloudEvent>())

data class User(val name: String)
//data class User(val name: String, val ready: Boolean)