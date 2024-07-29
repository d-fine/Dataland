package org.dataland.frameworktoolbox.intermediate.components.basecomponents

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.qamodel.addQaPropertyWithDocumentSupport

/**
 * A simple Kotlin-backed base component that generates a data model and QA model with a single property
 * based on a Kotlin type.
 */
open class SimpleKotlinBackedBaseComponent(
    identifier: String,
    parent: FieldNodeParent,
    var fullyQualifiedNameOfKotlinType: String,
) : ComponentBase(identifier, parent) {
    // This method has been marked final to prevent accidental overwriting of generateDefaultDataModel
    // without overwriting generateDefaultQaModel.
    // If you need to overwrite this method, please extend from ComponentBase
    final override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(fullyQualifiedNameOfKotlinType, isNullable),
        )
    }

    // This method has been marked final to prevent accidental overwriting of generateDefaultQaModel
    // without overwriting generateDefaultDataModel.
    // If you need to overwrite this method, please extend from ComponentBase
    final override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addQaPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(fullyQualifiedNameOfKotlinType, isNullable),
        )
    }
}
