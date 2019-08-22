package io.holunda.talk.camundacon

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableProcessApplication
class CamundaApplication


fun main(args: Array<String>) = runApplication<CamundaApplication>(*args).let { Unit }
