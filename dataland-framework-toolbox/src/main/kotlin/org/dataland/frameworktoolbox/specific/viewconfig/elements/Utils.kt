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
                // Normally: you would expect the condition: if (it.isNullable) "?" else ""
                // However, due to a previous bug in it.isNullable,
                // a "?" was always emitted and some code paths depend on this.
                // So we always output a "?".
                is ComponentGroup -> it.identifier + "?"
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

/**
 * Assuming standard kotlin-dataclass generation, this function generates a null-safe accessor
 * from the root dataset object to the field in Kotlin
 */
fun ComponentBase.getKotlinFieldAccessor(valueAccessor: Boolean = false): String {
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
