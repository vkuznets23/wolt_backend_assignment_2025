package com.example.dopc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.cache.annotation.EnableCaching

@EnableRetry @EnableCaching @SpringBootApplication class DopcApplication

fun main(args: Array<String>) {
    runApplication<DopcApplication>(*args)
}
