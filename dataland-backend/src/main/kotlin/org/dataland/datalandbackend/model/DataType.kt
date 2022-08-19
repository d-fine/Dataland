package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.datalandbackend.annotations.DataTypesExtractor
import org.dataland.datalandbackend.annotations.DataType as DataTypeAnnotation

/**
 * This is a utility class that encapsultes the name of DataTypes.
 * During construction it is verified, that the name actually represents a datatype.
 * But this class is Serialised and Deserialized as a simple string
 * The DataTypeSchemaCustomizer ensures that this property gets displayed as an Enum in the OpenApi spec
 */
data class DataType @JsonCreator constructor(
    @JsonValue
    val name: String
) {
    companion object {
        private val allowedDataTypes = DataTypesExtractor().getAllDataTypes()

        /**
         * Resolves a string to a valid datatype (ignoring case as that would otherwise sometimes cause issues)
         */
        @JsonCreator
        @JvmStatic
        fun valueOf(input: String): DataType {
            val str = allowedDataTypes.find {
                it.equals(input, ignoreCase = true)
            } ?: throw IllegalArgumentException("$input is not a recognised dataType")
            return DataType(str)
        }

        /**
         * Resolves a class to the corresponding DataType
         */
        fun of(clazz: Class<*>): DataType {
            return DataType(clazz.getAnnotation(DataTypeAnnotation::class.java).name)
        }
    }

    init {
        if (!allowedDataTypes.contains(name)) {
            throw IllegalArgumentException("$name is not a recognised dataType")
        }
    }

    override fun toString(): String {
        return name
    }
}
