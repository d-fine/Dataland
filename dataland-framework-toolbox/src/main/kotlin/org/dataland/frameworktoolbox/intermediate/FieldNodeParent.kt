package org.dataland.frameworktoolbox.intermediate

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase

/**
 * A FieldNodeParent is the potential parent of a FieldNode
 * Examples include the ComponentGroup and TopLevelComponentGroup
 */
interface FieldNodeParent {
    val children: Sequence<ComponentBase>
    val nestedChildren: Sequence<ComponentBase>
        get() =
            children.flatMap {
                if (it is FieldNodeParent) {
                    listOf(it) + it.nestedChildren
                } else {
                    listOf(it)
                }
            }
}
