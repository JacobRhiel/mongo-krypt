package com.mongokrypt.shared

import com.mongokrypt.shared.schema.algorithm.EncryptionAlgorithm

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 16, 2021
 */
object MongoKryptConstants {

    val possibleAlgorithms = setOf(EncryptionAlgorithm.Deterministic::class, EncryptionAlgorithm.Random::class)

}