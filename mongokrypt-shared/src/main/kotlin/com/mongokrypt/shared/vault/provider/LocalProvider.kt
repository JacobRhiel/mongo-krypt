package com.mongokrypt.shared.vault.provider

import com.mongodb.client.model.vault.DataKeyOptions
import com.mongokrypt.shared.vault.configuration.local.LocalProviderConfiguration

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 17, 2021
 */
class LocalProvider(
    uri: String,
    namespace: String,
    configuration: LocalProviderConfiguration
) : AbstractKeyProvider<LocalProviderConfiguration>(
    configuration, uri, namespace,
    mapOf(
        "local" to mapOf(
            "key" to configuration.masterKey.readBytes()
        )
    )
) {

    override val keyOptions: DataKeyOptions = DataKeyOptions()

    override fun createKey(): String = createKey("local")

    override fun createKey(provider: String): String {
        return super.createKey(provider, keyOptions)
    }

    override fun createKey(provider: String, keyOptions: DataKeyOptions): String {
        return super.createKey(provider, this.keyOptions)
    }

    override fun createKey(provider: String, keyOptions: DataKeyOptions.() -> Unit): String {
        return super.createKey(provider, this.keyOptions)
    }
}