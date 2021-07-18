package com.mongokrypt.shared.vault.provider

import com.mongokrypt.shared.schema.JsonSchema

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 12, 2021
 */
class LocalKeyProvider(
    uri: String,
    namespace: String,
   // override val database: String,
   // override val masterKey: ByteArray
) /*: AbstractKeyProvider(uri, namespace, mapOf(
    "local" to mapOf(
        "key" to masterKey
    )
))*/ {

    //todo check if already exists
   /* fun mapSchemaToEncryptionSettings(schema: Array<JsonSchema>) {
        autoEncryptionSettings.schemaMap.putAll(schema.map { database to it.toBson() })
    }

    override fun createKey(): String {
        TODO("Not yet implemented")
    }*/

}