package com.mongokrypt.shared.schema.generator

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 17, 2021
 */
enum class PreloadType {
    INDIVIDUAL,
    GROUP,
    BINARY
    ;

    companion object {
        fun findByName(name: String) = values().firstOrNull {
            it.name == name
        } ?: GROUP
    }

}