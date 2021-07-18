package com.mongokrypt.shared.vault.provider

import com.mongodb.AutoEncryptionSettings
import com.mongodb.ClientEncryptionSettings
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.vault.DataKeyOptions
import com.mongodb.client.vault.ClientEncryptions
import com.mongokrypt.shared.vault.configuration.IProviderConfiguration
import java.util.*

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 13, 2021
 */
abstract class AbstractKeyProvider<C : IProviderConfiguration>(
    override val configuration: C,
    final override val uri: String,
    final override val namespace: String,
    final override val kmsProviders: Map<String, Map<String, Any>>
) : IKeyProvider<C> {

    override val encryptionSettings: ClientEncryptionSettings = ClientEncryptionSettings.builder()
        .keyVaultMongoClientSettings(
            MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(uri))
                .build()
        ).keyVaultNamespace(namespace)
        .kmsProviders(kmsProviders)
        .build()

    override var autoEncryptionSettings: AutoEncryptionSettings = AutoEncryptionSettings.builder()
        .keyVaultNamespace(namespace)
        .kmsProviders(kmsProviders)
        .build()

    abstract fun createKey(): String

    open fun createKey(provider: String): String = createKey(provider)

    open fun createKey(provider: String, keyOptions: DataKeyOptions.() -> Unit): String {
        val options = DataKeyOptions()
        keyOptions(options)
        return createKey(provider, options)
    }

    open fun createKey(provider: String, keyOptions: DataKeyOptions): String {
        val client = ClientEncryptions.create(encryptionSettings)
        val binary = client.createDataKey(provider, keyOptions)
        return Base64.getEncoder().encodeToString(binary.data) ?: ""
    }
}