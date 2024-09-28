package com.creative_it.meetup_game_server

data class Game(val id: String, val users: MutableList<User> = mutableListOf())

data class User(val name: String, val ready: Boolean)