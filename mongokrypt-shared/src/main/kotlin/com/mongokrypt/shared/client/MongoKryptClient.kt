package com.mongokrypt.shared.client

import com.mongodb.client.MongoClient

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 18, 2021
 */
data class MongoKryptClient(
    val internalClient: MongoClient
) : MongoClient by internalClient