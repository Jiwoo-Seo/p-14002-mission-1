package com.back

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class BackApplication {

    fun main(args: Array<String>) {
        SpringApplication.run(BackApplication::class.java, *args)
    }
}
