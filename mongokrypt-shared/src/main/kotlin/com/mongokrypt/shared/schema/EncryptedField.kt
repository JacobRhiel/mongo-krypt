package com.mongokrypt.shared.schema

import com.mongokrypt.shared.schema.algorithm.EncryptionAlgorithm
import kotlin.reflect.KClass

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 11, 2021
 */
@Target(AnnotationTarget.FIELD)
annotation class EncryptedField(
    val algorithm: KClass<out EncryptionAlgorithm> = EncryptionAlgorithm.Deterministic::class,
    val followNesting: Boolean = true
)
