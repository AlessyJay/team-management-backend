package com.example.team_management_backend.Entities

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(attribute: List<String>?): String? =
        attribute?.filter { it.isNotBlank() }?.joinToString(",")?.ifBlank { null }

    override fun convertToEntityAttribute(dbData: String?): List<String> =
        dbData?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
}