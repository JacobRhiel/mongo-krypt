package com.mongokrypt.shared.vault

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.vault.DataKeyOptions
import com.mongokrypt.configuration.MongoKryptConfiguration
import com.mongokrypt.shared.schema.JsonSchema
import com.mongokrypt.shared.schema.generator.MongoEncryptedSchemaGenerator
import com.mongokrypt.shared.vault.configuration.KeyVaultConfiguration
import com.mongokrypt.shared.vault.provider.AbstractKeyProvider
import com.mongokrypt.utilities.JacksonUtils.mapper
import com.mongokrypt.utilities.logging.logger

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 13, 2021
 */
open class KeyVault(
    val mongoConfiguration: MongoKryptConfiguration,
    val schemaGenerator: MongoEncryptedSchemaGenerator? = null
) {
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

    inline fun <reified T : AbstractKeyProvider<*>> createKey() {
        mongoConfiguration.getProvider<T>().createKey()
    }

    inline fun <reified T : AbstractKeyProvider<*>> createKey(provider: String) {
        mongoConfiguration.getProvider<T>().createKey(provider)
    }

    inline fun <reified T : AbstractKeyProvider<*>> createKey(provider: String, keyOptions: DataKeyOptions) {
        mongoConfiguration.getProvider<T>().createKey(provider, keyOptions)
    }

    inline fun <reified T : AbstractKeyProvider<*>> createKey(provider: String, noinline keyOptions: DataKeyOptions.() -> Unit) {
        mongoConfiguration.getProvider<T>().createKey(provider, keyOptions)
    }

}