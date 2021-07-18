package com.mongokrypt.shared.schema.algorithm

import kotlin.reflect.KClass

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 16, 2021
 */
sealed class EncryptionAlgorithm(
    val representation: String
) {
    object Deterministic : EncryptionAlgorithm("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic")
    object Random : EncryptionAlgorithm("AEAD_AES_256_CBC_HMAC_SHA_512-Random")

    companion object {

        fun forClass(clazz: KClass<out EncryptionAlgorithm>): EncryptionAlgorithm? {
            return EncryptionAlgorithm::class.nestedClasses.firstOrNull { it == clazz }?.objectInstance
                    as? EncryptionAlgorithm
        }

        fun findByName(name: String) = EncryptionAlgorithm::class.nestedClasses
            .firstOrNull {
                it::class.simpleName?.lowercase() == name.lowercase()
            }?.objectInstance as? EncryptionAlgorithm ?: Deterministic

    }

}
