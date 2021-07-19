package com.mongokrypt.shared.vault.provider

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.mongodb.AutoEncryptionSettings
import com.mongodb.ClientEncryptionSettings
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.vault.DataKeyOptions
import com.mongodb.client.vault.ClientEncryption
import com.mongodb.client.vault.ClientEncryptions
import com.mongokrypt.shared.schema.JsonSchema
import com.mongokrypt.shared.utilities.JacksonUtils
import com.mongokrypt.shared.vault.configuration.IProviderConfiguration
import org.bson.BsonDocument
import java.io.File
import java.util.*

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 13, 2021
 */
abstract class AbstractKeyProvider<C : IProviderConfiguration>(
    override val configuration: C,
    final override val uri: String,
    final override val namespace: String,
    final override val kmsProviderKey: String,
    final override val kmsProvider: Map<String, Any>
) : IKeyProvider<C> {

    private val kmsKeyMap = mapOf(kmsProviderKey to kmsProvider)

    final override val encryptionSettings: ClientEncryptionSettings = ClientEncryptionSettings.builder()
        .keyVaultMongoClientSettings(
            MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(uri))
                .build()
        ).keyVaultNamespace(namespace)
        .kmsProviders(kmsKeyMap)
        .build()

    override var autoEncryptionSettings: AutoEncryptionSettings = AutoEncryptionSettings.builder()
        .keyVaultNamespace(namespace)
        .kmsProviders(kmsKeyMap)
        .build()

    private val internalEncryptionClient: ClientEncryption = ClientEncryptions.create(encryptionSettings)

    abstract fun createKey(): String

    open fun createKey(provider: String): String = createKey(provider)

    open fun createKey(provider: String, keyOptions: DataKeyOptions.() -> Unit): String {
        val options = DataKeyOptions()
        keyOptions(options)
        return createKey(provider, options)
    }

    open fun createKey(provider: String, keyOptions: DataKeyOptions): String {
        val binary = internalEncryptionClient.createDataKey(provider, keyOptions)
        return Base64.getEncoder().encodeToString(binary.data) ?: ""
    }

    override fun createEncryptedClient(schemas: Map<String, BsonDocument>): MongoClient {
        val encryptionSettings = AutoEncryptionSettings.builder()
            .keyVaultNamespace(namespace)
            .kmsProviders(kmsKeyMap)
            .schemaMap(schemas)
            .extraOptions(mapOf("mongocryptdBypassSpawn" to true))
            .build()
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(uri))
            .autoEncryptionSettings(encryptionSettings)
            .build()
        return MongoClients.create(settings)
    }

    fun preloadSchemas(file: File): Map<String, JsonSchema> {
        if(!file.isFile) throw Exception("Cannot load schemas from specified file: [$file].")
        val mapper = JacksonUtils.mapper
        val parser = mapper.createParser(file)
        val node: JsonNode = parser.codec.readTree(parser)
        val groupedSchema = mapper.convertValue(node, object : TypeReference<Map<String, Any>>() {})
        val schemaCache = mutableMapOf<String, JsonSchema>()
        groupedSchema.forEach { (key, value) ->
            val schemaNode = mapper.createObjectNode()
            schemaNode.putPOJO(key, value)
            val identifier = key.split(".")
            val collection = identifier[1]
            println(collection)
            if(schemaCache.containsKey(collection))
                throw Exception("Duplicate collection schema found in schema preload. [$collection].")
            schemaCache[collection] = JsonSchema(schemaNode.toPrettyString())
        }
        return schemaCache.toMap()
    }

}