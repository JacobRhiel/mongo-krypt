package com.mongokrypt.utilities

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 15, 2021
 */
object JacksonUtils {

    val mapper = ObjectMapper().registerModules(
        KotlinModule(), AfterburnerModule()
    )

}