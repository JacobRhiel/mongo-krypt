package com.mongokrypt.shared.vault.configuration.exception

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 15, 2021
 */
data class MissingConfigurationFieldException(
    val fieldName: String,
    val configuration: String = ""
) : Exception() {



}