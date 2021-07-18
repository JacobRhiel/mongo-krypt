package com.mongokrypt.shared.schema.dumper

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 16, 2021
 */
enum class DumpPolicy {
    NONE,
    INDIVIDUAL,
    GROUP,
    BINARY
    ;

    companion object {
        fun findByName(name: String) = values().firstOrNull {
            it.name == name
        } ?: NONE
    }

}