package org.dataland.rabbitmq

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class ProducerApplication

fun main(args: Array<String>) {
    runApplication<ProducerApplication>(*args)
}

@RestController
class RabbitProduceController(val rabbitTemplate: RabbitTemplate) {
    @PostMapping("/person/{name}")
    fun postPerson(@PathVariable name: String): ResponseEntity<String> {
        rabbitTemplate.convertAndSend("upload_queue", name)
        return ResponseEntity.ok(name)
    }
}
