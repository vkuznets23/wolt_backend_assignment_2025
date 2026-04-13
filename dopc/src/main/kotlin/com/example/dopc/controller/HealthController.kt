package com.example.dopc.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController() {

    @GetMapping("/") fun root(): String = "DOPC API is running"

    @GetMapping("/health")
    fun health(): String {
        return "OK"
    }
}
