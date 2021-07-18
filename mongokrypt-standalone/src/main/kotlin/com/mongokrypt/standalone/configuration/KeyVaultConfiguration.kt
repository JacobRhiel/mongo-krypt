package com.mongokrypt.standalone.configuration

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.mongokrypt.shared.vault.configuration.IKeyVaultConfiguration
import com.mongokrypt.shared.vault.configuration.KeyConfig
import com.mongokrypt.shared.vault.configuration.MongoKryptConfigurationDeserializer

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 13, 2021
 */
@JsonDeserialize(using = MongoKryptConfigurationDeserializer::class)
data class KeyVaultConfiguration(
    override val uri: String,
    override val database: String,
    override val namespace: String,
    override val keys: List<KeyConfig> = emptyList()
) : IKeyVaultConfiguration
