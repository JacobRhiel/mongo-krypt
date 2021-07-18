package com.mongokrypt.shared.vault.configuration

import com.mongokrypt.shared.schema.generator.MongoEncryptedSchemaGenerator

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 13, 2021
 */
interface IKeyVaultConfiguration {
    val uri: String
    val database: String
    val namespace: String
    val keys: List<KeyConfig>
        get() = emptyList()
}