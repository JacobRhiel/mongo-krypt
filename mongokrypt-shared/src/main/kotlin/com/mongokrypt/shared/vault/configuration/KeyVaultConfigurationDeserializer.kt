package com.mongokrypt.shared.vault.configuration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.mongokrypt.shared.utilities.JacksonUtils
import java.io.IOException

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 16, 2021
 */
class KeyVaultConfigurationDeserializer : StdDeserializer<IKeyVaultConfiguration>(IKeyVaultConfiguration::class.java) {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): IKeyVaultConfiguration? {
        val node: JsonNode = parser.codec.readTree(parser)
        val requiredFieldNames = listOf("uri", "namespace", "database")
        val missingFields = mutableListOf<String>()
        requiredFieldNames.forEach { fieldName ->
            if (!node.hasNonNull(fieldName))
                missingFields.add(fieldName)
        }
        if (missingFields.isNotEmpty())
            throw Exception("Missing field names: $missingFields")
        return KeyVaultConfiguration(
            node["uri"].textValue(),
            node["database"].textValue(),
            node["namespace"].textValue(),
            JacksonUtils.mapper.convertValue(node["keys"], object : TypeReference<List<KeyConfig>>() {})
        )
    }

}
