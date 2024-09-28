package org.dataland.frameworktoolbox.template.model

import com.fasterxml.jackson.annotation.JsonCreator
import java.lang.IllegalArgumentException

/**
 * Different framework elements require different levels of proof / attestation
 * in the form of documents. This enum describes the different levels of document support that are available.
 */
enum class TemplateDocumentSupport {
    None,
    Simple,
    Extended,
    ;

    companion object {
        /**
         * Parse a string to a TemplateDocumentSupport enum. A blank string is treated as "None".
         */
        @JsonCreator
        @JvmStatic
        fun fromString(input: String): TemplateDocumentSupport =
            when (input.trim()) {
                "Simple" -> Simple
                "Extended" -> Extended
                "None", "" -> None
                else -> throw IllegalArgumentException(
                    "Cannot convert '$input' to a TemplateDocumentSupport value.",
                )
            }
    }
}
