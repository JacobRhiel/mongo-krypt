package com.mongokrypt.shared.schema.dumper

import com.fasterxml.jackson.databind.JsonNode
import com.mongokrypt.shared.schema.JsonSchema
import com.mongokrypt.shared.utilities.JacksonUtils
import com.mongokrypt.shared.utilities.logging.logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.pathString

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 17, 2021
 */
data class EncryptedSchemaDumper(
    val schemas: Map<String, JsonSchema> = emptyMap(),
    val database: String,
    val dumpPolicy: DumpPolicy = DumpPolicy.GROUP,
    val dumpPath: Path,
    val overwrite: Boolean = false
) {

    class Builder {
        private val schemas = mutableMapOf<String, JsonSchema>()
        private lateinit var database: String
        private var dumpPolicy = DumpPolicy.GROUP
        private var dumpPath: Path = Path.of("./schemas")
        private var overwrite: Boolean = false

        fun useSchemas(schemaCache: Map<String, JsonSchema>): Builder {
            this.schemas.putAll(schemaCache)
            return this
        }

        fun useDatabase(database: String): Builder {
            this.database = database
            return this
        }

        fun dumpPolicy(policy: DumpPolicy): Builder {
            dumpPolicy = policy
            return this
        }

        fun dumpPath(path: String) = dumpPath(Path.of(path))

        fun dumpPath(path: Path): Builder {
            dumpPath = path
            return this
        }

        fun overwrite(overwrite: Boolean): Builder {
            this.overwrite = overwrite
            return this
        }

        fun build(): EncryptedSchemaDumper {
            if(!this::database.isInitialized)
                throw Exception("Database has not been defined.")
            return EncryptedSchemaDumper(schemas, database, dumpPolicy, dumpPath, overwrite)
        }
    }

    fun dumpSchema(collection: String, schema: JsonSchema) = dumpIndividualSchemas(
        mapOf(collection to schema)
    )

    fun dumpSchemas() = dumpSchemas(dumpPolicy)

    fun dumpSchemas(schemas: Map<String, JsonSchema>) = dumpSchemas(dumpPolicy, schemas)

    fun dumpSchemas(policy: DumpPolicy) = dumpSchemas(policy, schemas)

    fun dumpSchemas(policy: DumpPolicy, schemas: Map<String, JsonSchema>) {
        if(!overwrite) return
        return when(policy) {
            DumpPolicy.NONE -> return
            DumpPolicy.INDIVIDUAL -> dumpIndividualSchemas(schemas)
            DumpPolicy.GROUP -> dumpSchemasAsGroup(schemas = schemas)
            DumpPolicy.BINARY -> throw Exception("Not currently implemented.")
        }
    }

    private fun dumpIndividualSchemas() {
        if(schemas.isEmpty())
            logger().warn("No schemas defined in schema dumper.")
                .also { return }
        return dumpIndividualSchemas(schemas)
    }

    private fun dumpIndividualSchemas(schemas: Map<String, JsonSchema>) {
        schemas.forEach { (collection, schema) ->
            val path = dumpPath.resolve("individual").resolve(collection)
            path.createDirectories()
            val file = File(path.resolve("schema").pathString.plus(".json"))
            Files.write(file.toPath(), schema.toString().toByteArray())
        }
    }

    private fun dumpSchemasAsGroup(fileName: String = "$database-schema") {
        if(schemas.isEmpty())
            logger().warn("No schemas defined in schema dumper.")
                .also { return }
        return dumpSchemasAsGroup(fileName, schemas)
    }

    private fun dumpSchemasAsGroup(
        fileName: String = "$database-schema",
        schemas: Map<String, JsonSchema>
    ) {
        dumpPath.createDirectories()
        val file = File(dumpPath.pathString.plus("/${fileName}.json"))
        val mapper = JacksonUtils.mapper
        val rootNode = mapper.createObjectNode()
        if(schemas.isEmpty())
            logger().warn("No schemas defined in schema dumper.")
                .also { return }
        schemas.forEach { (collection, schema) ->
            val factory = mapper.factory
            val parser = factory.createParser(schema.toString())
            val node = parser.readValueAsTree<JsonNode>()
            val innerSchemaNode = node.get(database.plus(".").plus(collection))
            rootNode.putPOJO(database.plus(".").plus(collection), innerSchemaNode)
        }
        logger().info { "writing" }
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode)
    }

}