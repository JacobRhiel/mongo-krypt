package com.mongokrypt.shared.vault.configuration

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 13, 2021
 */
data class KeyConfig(
    val collection: String,
    val key: String,
    val aliases: Array<String> = emptyArray()
) {
    constructor() : this("", "", arrayOf())

    companion object {
        val EMPTY = KeyConfig()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyConfig

        if (collection != other.collection) return false
        if (key != other.key) return false
        if (!aliases.contentEquals(other.aliases)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = collection.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + aliases.contentHashCode()
        return result
    }

}
