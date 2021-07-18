package com.mongokrypt.shared.schema

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 12, 2021
 */
@Target(AnnotationTarget.CLASS)
annotation class EncryptionKey(
    val alias: String,
    val uuid: Boolean = false,
    val autoGenerate: Boolean = true
)
