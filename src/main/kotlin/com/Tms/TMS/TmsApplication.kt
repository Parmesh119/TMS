package com.Tms.TMS

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication// Add if needed
class TmsApplication

fun main(args: Array<String>) {
	runApplication<TmsApplication>(*args)
}