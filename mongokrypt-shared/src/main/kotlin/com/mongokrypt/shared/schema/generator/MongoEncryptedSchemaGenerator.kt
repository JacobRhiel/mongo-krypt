package com.mongokrypt.shared.schema.generator

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.mongokrypt.shared.MongoKryptConstants.possibleAlgorithms
import com.mongokrypt.shared.schema.*
import com.mongokrypt.shared.schema.algorithm.EncryptionAlgorithm
import com.mongokrypt.shared.schema.dumper.EncryptedSchemaDumper
import com.mongokrypt.shared.schema.generator.PreloadType.*
import com.mongokrypt.shared.vault.provider.AbstractKeyProvider
import com.mongokrypt.utilities.JacksonUtils.mapper
import io.github.classgraph.ClassGraph
import java.lang.reflect.Field
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 12, 2021
 */
class MongoEncryptedSchemaGenerator(
    val database: String,
    private val ignoredTypes: MutableList<Class<*>> = mutableListOf(),
    private val dumper: EncryptedSchemaDumper? = null
) {
    private val schemaCache = mutableMapOf<String, JsonSchema>()

    class Builder {
        private var ignoredTypes: MutableList<Class<*>> = mutableListOf()
        private lateinit var database: String
        private var dumper: EncryptedSchemaDumper? = null

        fun addIgnoredType(clazz: Class<*>): Builder {
            if (!ignoredTypes.contains(clazz))
                ignoredTypes.add(clazz)
            return this
        }

        fun removeIgnoredType(clazz: Class<*>): Builder {
            if (ignoredTypes.contains(clazz))
                ignoredTypes.remove(clazz)
            return this
        }

        fun useDatabase(database: String): Builder {
            this.database = database
            return this
        }

        fun useDumper(dumper: EncryptedSchemaDumper): Builder {
            this.dumper = dumper
            return this
        }

        fun build(): MongoEncryptedSchemaGenerator {
            if (!this::database.isInitialized)
                throw Exception("Database not set for schema generator.")
            return MongoEncryptedSchemaGenerator(database, ignoredTypes, dumper)
        }
    }

    fun generateAll(
        provider: AbstractKeyProvider<*>,
        vararg ignoredTypes: Class<*> = emptyArray()
    ): Map<String, JsonSchema> {
        this.ignoredTypes.addAll(ignoredTypes)
        val loadedSchemas = mutableMapOf<String, JsonSchema>()
        ClassGraph().enableAllInfo().scan().use { result ->
            val classes = result.getClassesWithAnnotation(EncryptedSchema::class.qualifiedName).loadClasses()
            classes.forEach { clazz ->
                val schema = generate(clazz, provider) ?: return@forEach
                val encryptedSchema = clazz.getAnnotation(EncryptedSchema::class.java)
                if (schemaCache.containsKey(encryptedSchema.collectionName))
                    return@forEach
                if (loadedSchemas.containsKey(encryptedSchema.collectionName))
                    throw Exception(
                        "Duplicate schema attempt: ${encryptedSchema.collectionName} in" +
                                "[${clazz.simpleName}]."
                    )
                loadedSchemas[encryptedSchema.collectionName] = schema.second
                schemaCache.putIfAbsent(encryptedSchema.collectionName, schema.second)
            }
        }
        return schemaCache.also {
            dumper?.dumpSchemas(it)
                .also { println("dumped") }
        }
    }

    fun generate(
        clazz: Class<*>,
        provider: AbstractKeyProvider<*>,
        vararg ignoredTypes: Class<*> = emptyArray()
    ): Pair<String, JsonSchema>? {
        this.ignoredTypes.addAll(ignoredTypes)
        val hasSchemaAnnotation = clazz.isAnnotationPresent(EncryptedSchema::class.java)
        if (!hasSchemaAnnotation) return "" to JsonSchema.EMPTY
        val schemaAnnotation = clazz.getAnnotation(EncryptedSchema::class.java)
        if (schemaCache.containsKey(schemaAnnotation.collectionName))
            return null
        val encryptionKey =
            if (clazz.isAnnotationPresent(EncryptionKey::class.java))
                clazz.getAnnotation(EncryptionKey::class.java)
            else null
        if (encryptionKey == null) return "" to JsonSchema.EMPTY
        val node = createCollectionSchema(clazz, provider, encryptionKey, schemaAnnotation)
        val schema = JsonSchema(node.toPrettyString())
        return schemaAnnotation.collectionName to schema
    }

    inline fun <reified T : Class<*>> createCollectionSchema(
        clazz: T,
        provider: AbstractKeyProvider<*>,
        encryptionKey: EncryptionKey,
        schemaAnnotation: EncryptedSchema,
    ): ObjectNode {
        val schemaObject = mapper.createObjectNode()
        val base64Key = provider.createKey()
        val schemaEntry = mapper.createObjectNode()
        schemaEntry.createSchemaMetadata(base64Key, encryptionKey, schemaAnnotation)
        schemaEntry.createSchemaObject(schemaAnnotation = schemaAnnotation, properties = clazz.declaredFields.toList())
        schemaObject.putPOJO(database.plus(".").plus(schemaAnnotation.collectionName), schemaEntry)
        return schemaObject
    }

    fun ArrayNode.createKeyData(key: String, encryptionKey: EncryptionKey) {
        val mapper = mapper
        val data = mapper.createObjectNode()
        if (encryptionKey.uuid) {
            add("UUID(${key})")
        } else {
            val binary = mapper.createObjectNode()
            println("key: $key")
            binary.put("base64", key)
            binary.put("subType", "04")
            data.putPOJO("\$binary", binary)
            add(data)
        }
    }

    fun ObjectNode.createSchemaMetadata(
        key: String,
        encryptionKey: EncryptionKey,
        schemaAnnotation: EncryptedSchema
    ): ObjectNode {
        val mapper = mapper
        val metadata = mapper.createObjectNode()
        with(metadata) {
            putArray("keyId").createKeyData(key, encryptionKey)
            val algorithm = schemaAnnotation.defaultAlgorithm
            if (!EncryptionAlgorithm::class.isSuperclassOf(algorithm))
                throw Exception("Attempting to use an algorithm that is not supported. [$algorithm]")
            put("algorithm", EncryptionAlgorithm.forClass(algorithm)!!.representation)
        }
        putPOJO("encryptMetadata", metadata)
        return this
    }

    fun ObjectNode.createSchemaObject(
        type: String = "object",
        schemaAnnotation: EncryptedSchema,
        properties: List<Field>
    ): ObjectNode {
        val mapper = mapper
        val propertiesNode = mapper.createObjectNode()
        val ignoredTypeCondition = ignoredTypes.isNotEmpty()
        properties.filter {
            getObjectTypeForField(it) != "boolean"
                    && !it.isAnnotationPresent(IgnoreEncryption::class.java)
                    || (ignoredTypeCondition && ignoredTypes.contains(it.type))
        }.forEach { field ->
            println(schemaAnnotation.onlyAnnotatedFields)
            val isAnnotated = field.isAnnotationPresent(EncryptedField::class.java)
            if (schemaAnnotation.onlyAnnotatedFields) {
                if (isAnnotated) {
                    propertiesNode.createSchemaObjectProperty(
                        schemaAnnotation, field, field.getAnnotation(EncryptedField::class.java).algorithm
                    )
                }
            } else {
                if (isAnnotated) {
                    propertiesNode.createSchemaObjectProperty(
                        schemaAnnotation, field, field.getAnnotation(EncryptedField::class.java).algorithm
                    )
                } else propertiesNode.createSchemaObjectProperty(schemaAnnotation, field)
            }
        }
        put("bsonType", type)
        if (type != "array" && !propertiesNode.isEmpty)
            set<ObjectNode>("properties", propertiesNode)
        return this
    }

    private fun ObjectNode.createSchemaObjectProperty(
        schemaAnnotation: EncryptedSchema,
        field: Field,
        algorithm: KClass<out EncryptionAlgorithm>? = null,
        keyId: String? = null
    ): ObjectNode {
        val mapper = mapper
        val type = getObjectTypeForField(field)
        if (type == "object" || type == "array")
            putPOJO(field.name, mapNestedSchemaObject(type, schemaAnnotation, field))
        else {
            val encryptedObject = mapper.createObjectNode()
            val encryptedProperty = mapper.createObjectNode()
            with(encryptedProperty) {
                put("bsonType", type)
                if (algorithm != null && algorithm in possibleAlgorithms)
                    put("algorithm", EncryptionAlgorithm.forClass(algorithm)?.representation)
                if (keyId != null)
                    put("keyId", keyId)//todo: verification uuid check
            }
            with(encryptedObject) {
                replace("encrypt", encryptedProperty)
            }
            putPOJO(field.name, encryptedObject)
        }
        return this
    }

    private fun mapNestedSchemaObject(
        type: String = "object",
        schemaAnnotation: EncryptedSchema,
        field: Field
    ): ObjectNode {
        val mapper = mapper
        val wrapperNode = mapper.createObjectNode()
        wrapperNode.createSchemaObject(
            type = type,
            schemaAnnotation = schemaAnnotation,
            properties = field.type.declaredFields.toList()
        )
        return wrapperNode
    }

    /*private fun preloadSchemas() {
        val schema = path.toFile()
        if (!schema.exists())
            throw Exception("No file exists to preload schemas with configured name.")
        try {
            val parser = mapper.createParser(schema)
            val node: JsonNode = parser.codec.readTree(parser)
            when (preloadType) {
                INDIVIDUAL -> {
                    val identifier = node.fieldNames().asSequence().first().split(".")
                    val collection = identifier[1]
                    schemaCache[collection] = JsonSchema(node.toPrettyString())
                }
                GROUP -> {
                    val groupedSchema =
                        mapper.convertValue(node, object : TypeReference<Map<String, Any>>() {})
                    groupedSchema.forEach { (key, value) ->
                        val schemaNode = mapper.createObjectNode()
                        schemaNode.putPOJO(key, value)
                        val identifier = key.split(".")
                        val collection = identifier[1]
                        println(collection)
                        schemaCache[collection] = JsonSchema(schemaNode.toPrettyString())
                    }
                }
                BINARY -> throw Exception("Binary preloading is not currently a feature.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/

    private val WRAPPER_TYPES = setOf(
        Boolean::class.java,
        Char::class.java,
        Byte::class.java,
        Short::class.java,
        Int::class.java,
        Long::class.java,
        Float::class.java,
        Double::class.java,
        String::class.java
    )

    private fun getObjectTypeForField(field: Field) = when (field.type) {
        is Collection<*> -> "array"
        else -> {
            if (field.type.interfaces.contains(Collection::class.java))
                "array"
            else if (!isWrapperType(field.type))
                "object"
            else
                field.type.simpleName
        }
    }.lowercase(Locale.getDefault())

    private fun isWrapperType(clazz: Class<*>): Boolean {
        return WRAPPER_TYPES.contains(clazz)
    }

}