package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.ComponentMarker
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.TreeNode
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * A component is a higher-level abstraction for framework elements. Components are arranged in a hierarchy
 * and typically correspond to a single question in a questionnaire.
 * @param identifier the camelCase identifier of the component
 * @param parent the parent node in the hierarchy
 */
@ComponentMarker
open class ComponentBase(
    val identifier: String,
    override val parent: FieldNodeParent,
) : TreeNode<FieldNodeParent> {

    /**
     * The dataModelGenerator allows users to operate the DataClass generation of this specific component instance
     */
    var dataModelGenerator: ((dataClassBuilder: DataClassBuilder) -> Unit)? = null

    /**
     * True iff this component is optional / accepts null values
     */
    var isNullable: Boolean = true

    /**
     * Obtain a list of all parents of this node until the root node
     */
    fun parents(): Sequence<FieldNodeParent> = sequence {
        var currentNode: Any? = parent
        while (currentNode is FieldNodeParent) {
            yield(currentNode)

            if (currentNode is TreeNode<*>) {
                currentNode = currentNode.parent
            } else {
                break
            }
        }
    }

    /**
     * Build this component instance into the provided Kotlin DataClass using the default
     * generator for this component
     */
    open fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        throw NotImplementedError("This component did not implement data model conversion.")
    }

    /**
     * Build this component instance into the provided Kotlin DataClass
     */
    fun generateDataModel(dataClassBuilder: DataClassBuilder) {
        return dataModelGenerator?.let { it(dataClassBuilder) } ?: generateDefaultDataModel(dataClassBuilder)
    }
}
