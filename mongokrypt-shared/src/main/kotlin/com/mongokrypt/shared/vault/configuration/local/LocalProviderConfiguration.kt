package com.mongokrypt.shared.vault.configuration.local

import com.mongokrypt.shared.vault.configuration.IProviderConfiguration
import java.io.File

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 17, 2021
 */
data class LocalProviderConfiguration(val masterKey: File) : IProviderConfiguration