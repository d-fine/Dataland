package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.datalandbackend.annotations.DataTypesExtractor
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackend.annotations.DataType as DataTypeAnnotation

/**
 * This is a utility class that encapsulates the name of DataTypes.
 * During construction, it is verified that the name actually represents a datatype.
 * But this class is Serialised and Deserialized as a simple string
 * The DataTypeSchemaCustomizer ensures that this property gets displayed as an Enum in the OpenApi spec
 */
data class DataType
    @JsonCreator
    constructor(
        @JsonValue
        val name: String,
    ) {
        companion object {
            private val allowedDataTypes = DataTypesExtractor().getAllDataTypes()

            /**
             * Resolves a string to a valid datatype (ignoring case as that would otherwise sometimes cause issues)
             */
            @JsonCreator
            @JvmStatic
            fun valueOf(input: String): DataType {
                val str =
                    allowedDataTypes.find {
                        it.equals(input, ignoreCase = true)
                    } ?: input
                return DataType(str)
            }

            /**
             * Resolves a class to the corresponding DataType
             */
            fun of(clazz: Class<*>): DataType = DataType(clazz.getAnnotation(DataTypeAnnotation::class.java).name)

            /**
             * Returns all possible values
             */
            val values: List<DataType>
                get() = allowedDataTypes.map { valueOf(it) }

            /**
             * Checks if a given string is a representation of valid data type
             */
            fun isDataType(testString: String) = values.any { it.toString() == testString }
        }

        init {
            if (!allowedDataTypes.contains(name)) {
                throw InvalidInputApiException(
                    "$name is not a recognised dataType",
                    "$name is not a valid dataType. Please consult the API Reference to find a list of allowed values",
                )
            }
        }

        override fun toString(): String = name
    }
