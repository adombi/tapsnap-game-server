package com.creative_it.meetup_game_server

import io.rsocket.core.RSocketServer
import io.rsocket.core.Resume
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import org.springframework.boot.rsocket.server.RSocketServerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.util.retry.Retry

@Configuration
class RSocketConfiguration {
    @Bean
    fun rSocketResume(): RSocketServerCustomizer {
        val resume = Resume()
            .sessionDuration(15.minutes.toJavaDuration())
//            .retry(Retry.fixedDelay(Long.MAX_VALUE, 5.seconds.toJavaDuration()))
        return RSocketServerCustomizer { rSocketServer: RSocketServer -> rSocketServer.resume(resume) }
    }
}