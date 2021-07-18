package com.mongokrypt.shared.vault.provider

import com.mongodb.AutoEncryptionSettings
import com.mongodb.ClientEncryptionSettings
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.model.vault.DataKeyOptions
import com.mongodb.client.vault.ClientEncryption
import com.mongodb.client.vault.ClientEncryptions
import com.mongokrypt.shared.vault.configuration.IProviderConfiguration
import java.util.*

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 12, 2021
 */
interface IKeyProvider<C : IProviderConfiguration> {
    val configuration: C
    val uri: String
    val namespace: String
    val masterKey: ByteArray
        get() = byteArrayOf()
    val kmsProviders: Map<String, Map<String, Any>>
    val encryptionSettings: ClientEncryptionSettings
    var autoEncryptionSettings: AutoEncryptionSettings
    val keyOptions: DataKeyOptions
        get() = DataKeyOptions()
}