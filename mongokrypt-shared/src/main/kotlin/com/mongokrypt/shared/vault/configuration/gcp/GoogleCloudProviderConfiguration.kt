package com.mongokrypt.shared.vault.configuration.gcp

import com.mongokrypt.shared.vault.configuration.IProviderConfiguration

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 15, 2021
 */
data class GoogleCloudProviderConfiguration(
    val email: String,
    val privateKey: String,
    val projectId: String,
    val location: String,
    val keyRing: String,
    val keyVersion: String,
    val keyName: String,
    val authEndpoint: String = "oauth2.googleapis.com",
    val callbackEndpoint: String = "cloudkms.googleapis.com"
) : IProviderConfiguration