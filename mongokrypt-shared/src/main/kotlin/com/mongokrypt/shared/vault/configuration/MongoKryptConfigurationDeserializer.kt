package com.mongokrypt.shared.vault.configuration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.mongokrypt.shared.configuration.MongoKryptConfiguration
import com.mongokrypt.shared.vault.configuration.exception.MissingConfigurationFieldException
import com.mongokrypt.shared.vault.configuration.gcp.GoogleCloudProviderConfiguration
import com.mongokrypt.shared.vault.configuration.local.LocalProviderConfiguration
import com.mongokrypt.shared.vault.provider.AbstractKeyProvider
import com.mongokrypt.shared.vault.provider.GoogleCloudProvider
import com.mongokrypt.shared.vault.provider.LocalProvider
import com.mongokrypt.shared.utilities.JacksonUtils.mapper
import java.io.IOException
import kotlin.reflect.KClass

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 15, 2021
 */
class MongoKryptConfigurationDeserializer :
    StdDeserializer<MongoKryptConfiguration>(MongoKryptConfiguration::class.java) {

    private val providerTypes = arrayOf("local", "gcp", "aws", "azure")

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): MongoKryptConfiguration {
        val node: JsonNode = parser.codec.readTree(parser)
        val hasVault = node.has("vault")
        if (!hasVault) throw MissingConfigurationFieldException("vault")
        val vaultSettings = node.get("vault")
        val vaultConfiguration = mapper.convertValue(vaultSettings, KeyVaultConfiguration::class.java)
        val hasProviders = node.has("providers")
        val providerConfiguration = if (hasProviders) {
            val providers = node.get("providers")
            defineProviderConfiguration(vaultConfiguration, providers)
        } else null
        return MongoKryptConfiguration(vaultConfiguration, providerConfiguration ?: emptyMap())
    }

    private fun defineProviderConfiguration(
        vaultConfiguration: KeyVaultConfiguration,
        providerNode: JsonNode
    ): MutableMap<KClass<out IProviderConfiguration>, AbstractKeyProvider<out IProviderConfiguration>> {
        val providerFields =
            mapper.convertValue(providerNode, object : TypeReference<Map<String, Any>>() {}).toMutableMap()
        val providerConfiguration =
            mutableMapOf<KClass<out IProviderConfiguration>, AbstractKeyProvider<out IProviderConfiguration>>()
        if (providerFields.keys.any { it in providerTypes }) {
            providerFields.keys.toSet().forEach {
                when (it) {
                    "gcp" -> {
                        val gcpNode = providerFields["gcp"] as? Map<*, *>
                        checkRequiredMapFields(
                            gcpNode,
                            listOf("email", "privateKey", "projectId", "keyRing", "keyName")
                        )
                        val configuration = convertToConfiguration<GoogleCloudProviderConfiguration>(gcpNode)
                        providerConfiguration[GoogleCloudProviderConfiguration::class] =
                            GoogleCloudProvider(vaultConfiguration.uri, vaultConfiguration.namespace, configuration)
                    }
                    "local" -> {
                        val localNode = providerFields["local"] as? Map<*, *>
                        checkRequiredMapFields(localNode, listOf("masterKey"))
                        val configuration = convertToConfiguration<LocalProviderConfiguration>(localNode)
                        providerConfiguration[LocalProviderConfiguration::class] =
                            LocalProvider(vaultConfiguration.uri, vaultConfiguration.namespace, configuration)
                    }
                    else -> return@forEach
                }
                providerFields.remove(it)
            }
        }
        if (providerFields.keys.isNotEmpty() && !providerFields.keys.any { it in providerTypes }) {
            println("Unknown provider types: ${providerFields.keys.toTypedArray().contentDeepToString()}.")
        }
        return providerConfiguration
    }

    private fun checkRequiredNodeFields(node: JsonNode, requiredFieldNames: List<String>): Boolean {
        val missingFields = mutableListOf<String>()
        requiredFieldNames.forEach { fieldName ->
            if (!node.hasNonNull(fieldName))
                missingFields.add(fieldName)
        }
        if (missingFields.isNotEmpty())
            throw Exception("Missing field names: $missingFields")
        return true
    }

    private fun checkRequiredMapFields(map: Map<*, *>?, requiredFieldNames: List<String>): Boolean {
        if (map.isNullOrEmpty()) return false
        val missingFields = mutableListOf<String>()
        requiredFieldNames.forEach { fieldName ->
            if (!map.containsKey(fieldName))
                missingFields.add(fieldName)
        }
        if (missingFields.isNotEmpty())
            throw Exception("Missing field names: $missingFields")
        return true
    }

    inline fun <reified T : IProviderConfiguration> convertToConfiguration(
        data: Any?
    ): T {
        return try {
            mapper.convertValue(data, T::class.java)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            val message = e.message ?: ""
            val pre = message.substringAfter("JSON property")
            val fieldName = pre.substringBefore("due to missing").trim()
            throw MissingConfigurationFieldException(fieldName, T::class::simpleName.name)
        } as T
    }

}