package com.example.dopc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry @SpringBootApplication class DopcApplication

fun main(args: Array<String>) {
    runApplication<DopcApplication>(*args)
}
