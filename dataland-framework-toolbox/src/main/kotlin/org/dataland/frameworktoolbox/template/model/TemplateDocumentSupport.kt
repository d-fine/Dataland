package org.dataland.frameworktoolbox.template.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Different framework elements require different levels of proof / attestation
 * in the form of documents. This enum describes the different levels of document support that are available.
 */
enum class TemplateDocumentSupport(@JsonValue val value: String) {
    None("None"),
    Simple("Simple"),
    Extended("Extended"),
}
