package com.tapsnap.game_server

import com.fasterxml.jackson.annotation.JsonIgnore
import io.cloudevents.CloudEvent
import reactor.core.publisher.Sinks

data class Game(val id: String,
                val users: MutableList<String> = mutableListOf(),
                val results: MutableMap<String, MutableList<Int>> = mutableMapOf(),
                @JsonIgnore val eventBus: Sinks.Many<CloudEvent> = Sinks.unsafe().many().multicast().directBestEffort(),
                @JsonIgnore val dashboardEventBus: Sinks.Many<CloudEvent> = Sinks.unsafe().many().replay().latest())

//data class User(val name: String)
//data class User(val name: String, val ready: Boolean)