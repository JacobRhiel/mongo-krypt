package com.mongokrypt.shared.vault.provider

import com.mongodb.client.model.vault.DataKeyOptions
import com.mongokrypt.shared.vault.configuration.IProviderConfiguration
import com.mongokrypt.shared.vault.configuration.gcp.GoogleCloudProviderConfiguration
import org.bson.BsonDocument
import org.bson.BsonString

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 13, 2021
 */
class GoogleCloudProvider(
    uri: String,
    namespace: String,
    configuration: GoogleCloudProviderConfiguration
) : AbstractKeyProvider<GoogleCloudProviderConfiguration>(configuration, uri, namespace,
    mapOf(
        "gcp" to mapOf(
            "email" to BsonString(configuration.email),
            "privateKey" to BsonString(configuration.privateKey),
            "endpoint" to BsonString(
                if(configuration.authEndpoint.isEmpty())
                    "oauth2.googleapis.com"
                else configuration.authEndpoint
            )
    )
)) {

    override val keyOptions: DataKeyOptions = DataKeyOptions()
        .masterKey(
            with(BsonDocument()) {
                append("provider", BsonString("gcp"))
                append("projectId", BsonString(configuration.projectId))
                append("location", BsonString(configuration.location))
                append("keyName", BsonString(configuration.keyName))
                append("keyRing", BsonString(configuration.keyRing))
                append("keyVersion", BsonString(configuration.keyVersion))
                append("endpoint", BsonString(
                    if(configuration.callbackEndpoint.isEmpty())
                        "cloudkms.googleapis.com"
                    else configuration.callbackEndpoint))
            }
        )

    override fun createKey(): String = createKey("gcp")

    override fun createKey(provider: String): String {
        return super.createKey(provider, keyOptions)
    }

    override fun createKey(provider: String, keyOptions: DataKeyOptions): String {
        return super.createKey(provider, this.keyOptions)
    }

    override fun createKey(provider: String, keyOptions: DataKeyOptions.() -> Unit): String {
        return super.createKey(provider, this.keyOptions)
    }

}