package com.mongokrypt.utilities.logging

import mu.KotlinLogging

fun Any.logger() = KotlinLogging.logger(this::class.java.simpleName)