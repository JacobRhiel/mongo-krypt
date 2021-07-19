package com.mongokrypt.shared.vault

import com.mongodb.AutoEncryptionSettings
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.vault.DataKeyOptions
import com.mongokrypt.shared.client.MongoKryptClient
import com.mongokrypt.shared.configuration.MongoKryptConfiguration
import com.mongokrypt.shared.schema.JsonSchema
import com.mongokrypt.shared.schema.generator.MongoEncryptedSchemaGenerator
import com.mongokrypt.shared.vault.configuration.KeyVaultConfiguration
import com.mongokrypt.shared.vault.provider.AbstractKeyProvider
import com.mongokrypt.shared.utilities.JacksonUtils.mapper
import com.mongokrypt.shared.utilities.logging.logger
import com.mongokrypt.shared.vault.configuration.IKeyVaultConfiguration
import org.bson.BsonDocument
import java.io.File
import java.lang.Exception
import kotlin.reflect.KClass

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 13, 2021
 */
open class KeyVault(
    val mongoConfiguration: MongoKryptConfiguration,
    val schemaGenerator: MongoEncryptedSchemaGenerator? = null
): IKeyVaultConfiguration by mongoConfiguration.keyVaultConfiguration {
    private val configuration: KeyVaultConfiguration = mongoConfiguration.keyVaultConfiguration
    val generatedSchemas = mutableMapOf<String, JsonSchema>()
    private lateinit var internalClient: MongoClient

    private fun createClient() {
        internalClient = MongoClients.create(configuration.uri)
    }

    fun pullKeys() {
        if(!this::internalClient.isInitialized)
            createClient()
        val namespace = configuration.namespace.split(".")
        val keys = internalClient.getDatabase(namespace[0]).getCollection(namespace[1]).find().toList()
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(keys)
            .also { println(it) }
    }

    inline fun <reified T : AbstractKeyProvider<*>> generateSchema(
        clazz: Class<*>, vararg ignoredTypes: Class<*> = emptyArray()
    ): JsonSchema? {
        if(schemaGenerator == null)
            logger().warn("No schema generator defined for key vault.")
                .also { return null }
        val provider = mongoConfiguration.getProvider<T>()
        val schema = schemaGenerator!!.generate(clazz, provider, *ignoredTypes)
            ?: return null
        return generatedSchemas.putIfAbsent(schema.first, schema.second)
    }

    inline fun <reified T : AbstractKeyProvider<*>> generateSchemas(
        vararg ignoredTypes: Class<*> = emptyArray()
    ): Map<String, JsonSchema> {
        if(schemaGenerator == null)
            logger().warn("No schema generator defined for key vault.")
                .also { return emptyMap() }
        val provider = mongoConfiguration.getProvider<T>()
        val schemas = schemaGenerator!!.generateAll(provider, *ignoredTypes)
        schemas.filterTo(generatedSchemas) { !generatedSchemas.containsKey(it.key) }
        return generatedSchemas.toMap()
    }

    inline fun <reified T : AbstractKeyProvider<*>> loadSchemas(file: File): Map<String, JsonSchema> {
        if(mongoConfiguration.providerConfigurations.isEmpty()) return emptyMap()
        if(!file.isFile) throw Exception("Cannot load schemas from specified file: [$file].")
        val provider = mongoConfiguration.getProvider<T>()
        return provider.preloadSchemas(file)
    }

    inline fun <reified T : AbstractKeyProvider<*>> createKey(): String {
        return mongoConfiguration.getProvider<T>().createKey()
    }

    inline fun <reified T : AbstractKeyProvider<*>> createKey(provider: String): String {
        return mongoConfiguration.getProvider<T>().createKey(provider)
    }

    inline fun <reified T : AbstractKeyProvider<*>> createKey(provider: String, keyOptions: DataKeyOptions): String {
        return mongoConfiguration.getProvider<T>().createKey(provider, keyOptions)
    }

    inline fun <reified T : AbstractKeyProvider<*>> createKey(provider: String, noinline keyOptions: DataKeyOptions.() -> Unit): String {
        return mongoConfiguration.getProvider<T>().createKey(provider, keyOptions)
    }

    inline fun <reified T : List<KClass<out AbstractKeyProvider<*>>>> createEncryptedClient(providers: T, schemas: Map<String, BsonDocument>): MongoKryptClient {
        val providerMap = mongoConfiguration.getProviders(providers).map { it.kmsProviderKey to it.kmsProvider }.toMap()
        return MongoKryptClient(createMultiProviderEncryptedClient(providerMap, schemas))
    }

    fun createMultiProviderEncryptedClient(kmsProviders: Map<String, Map<String, Any>>, schemas: Map<String, BsonDocument>): MongoClient {
        val encryptionSettings = AutoEncryptionSettings.builder()
            .keyVaultNamespace(configuration.namespace)
            .kmsProviders(kmsProviders)
            .schemaMap(schemas)
            .extraOptions(mapOf("mongocryptdBypassSpawn" to true))
            .build()
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(configuration.uri))
            .autoEncryptionSettings(encryptionSettings)
            .build()
        return MongoClients.create(settings)
    }

}