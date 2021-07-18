package com.mongokrypt.shared.vault.provider

import com.mongokrypt.shared.vault.configuration.IProviderConfiguration
import com.mongokrypt.shared.vault.configuration.gcp.GoogleCloudProviderConfiguration
import com.mongokrypt.utilities.JacksonUtils

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 16, 2021
 */
object Providers {

    inline fun <reified T : Class<out IProviderConfiguration>> createProviderFromConfig(
        configuration: T
    ): T {
        val config = JacksonUtils.mapper.convertValue(configuration, configuration::class.java)
        val newClazz = when(config::class) {
            is Class<*> -> {

            }
            else -> throw Exception("unknown config type")
        }
        return newClazz as T
    }

}