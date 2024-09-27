package com.creative_it.meetup_game_server

import org.springframework.data.annotation.Id
import org.springframework.data.keyvalue.annotation.KeySpace

@KeySpace("games")
data class Game(@Id val id: String, val users: MutableList<User> = mutableListOf())

@KeySpace("users")
data class User(@Id val name: String, val ready: Boolean)