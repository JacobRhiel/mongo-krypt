package com.mongokrypt.shared.vault.configuration

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.mongokrypt.shared.schema.generator.MongoEncryptedSchemaGenerator

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 16, 2021
 */
@JsonDeserialize(using = KeyVaultConfigurationDeserializer::class)
data class KeyVaultConfiguration(
    override val uri: String,
    override val database: String,
    override val namespace: String,
    override val keys: List<KeyConfig> = emptyList()
) : IKeyVaultConfiguration