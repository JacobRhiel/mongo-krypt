package com.mongokrypt.shared.schema

import com.mongokrypt.shared.schema.algorithm.EncryptionAlgorithm
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class EncryptedSchema(
    val collectionName: String,
    val defaultAlgorithm: KClass<out EncryptionAlgorithm> = EncryptionAlgorithm.Deterministic::class,
    val applyAlgorithmToAllFields: Boolean = true,
    val onlyAnnotatedFields: Boolean = false
)