package org.dataland.frameworktoolbox.specific.viewconfig.elements

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup

/**
 * Assuming standard kotlin-dataclass generation, this function generates a null-safe accessor
 * from the root dataset object to the field in TypeScript
 */
fun ComponentBase.getTypescriptFieldAccessor(valueAccessor: Boolean = false): String {
    val parentsSequence = parents()
        .toList()
        .reversed()
        .mapNotNull {
            when (it) {
                is ComponentGroup -> it.identifier + if (it.isNullable) "?" else ""
                else -> null
            }
        }

    val dataPointAccessor = if (parentsSequence.isNotEmpty()) {
        "dataset.${parentsSequence.joinToString(".")}.$identifier"
    } else {
        "dataset.$identifier"
    }

    return if (valueAccessor) {
        documentSupport.getDataAccessor(dataPointAccessor, isNullable)
    } else {
        dataPointAccessor
    }
}
