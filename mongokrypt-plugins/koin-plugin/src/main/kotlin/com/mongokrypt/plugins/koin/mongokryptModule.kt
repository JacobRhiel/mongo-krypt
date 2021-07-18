package com.mongokrypt.plugins.koin

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.mongokrypt.configuration.MongoKryptConfiguration
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jul 17, 2021
 */
fun createMongokryptModule(path: String) = module {

    single { YAMLMapper.builder().build() } bind YAMLMapper::class

    single {
        val mapper: YAMLMapper by inject()
        mapper.readValue(File(path), MongoKryptConfiguration::class.java)
    }

}