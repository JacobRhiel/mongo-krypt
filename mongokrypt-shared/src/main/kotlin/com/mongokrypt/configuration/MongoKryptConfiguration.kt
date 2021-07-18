package com.mongokrypt.configuration

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.mongokrypt.shared.vault.configuration.IProviderConfiguration
import com.mongokrypt.shared.vault.configuration.KeyVaultConfiguration
import com.mongokrypt.shared.vault.configuration.MongoKryptConfigurationDeserializer
import com.mongokrypt.shared.vault.provider.AbstractKeyProvider
import kotlin.reflect.KClass

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 16, 2021
 */
@JsonDeserialize(using = MongoKryptConfigurationDeserializer::class)
data class MongoKryptConfiguration(
    val keyVaultConfiguration: KeyVaultConfiguration,
    val providerConfigurations: Map<KClass<out IProviderConfiguration>, AbstractKeyProvider<out IProviderConfiguration>>
) {

    inline fun <reified T: AbstractKeyProvider<*>> getProvider() = get(T::class)

    operator fun get(
        clazz: KClass<out AbstractKeyProvider<*>>
    ): AbstractKeyProvider<out IProviderConfiguration> {
        println(clazz)
        return providerConfigurations.values.firstOrNull {
            it::class == clazz
        } ?: throw Exception("No provider found for configuration.")
    }

}