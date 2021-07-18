package com.mongokrypt.shared.schema

import org.bson.BsonDocument

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 12, 2021
 */
@JvmInline
value class JsonSchema(private val json: String) {

    fun toBson() = BsonDocument.parse(json)

    override fun toString(): String {
        return json
    }

    companion object {
        val EMPTY = JsonSchema("")
    }
}