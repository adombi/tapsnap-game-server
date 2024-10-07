package com.creative_it.meetup_game_server

data class JoinRequest(val playerName: String)
data class React(val playerName: String, val respondTimeMillis: Int)
data class StartGame(val playerName: String)