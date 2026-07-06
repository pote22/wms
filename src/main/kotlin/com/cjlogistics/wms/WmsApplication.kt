package com.cjlogistics.wms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class WmsApplication

fun main(args: Array<String>) {
	runApplication<WmsApplication>(*args)
}
