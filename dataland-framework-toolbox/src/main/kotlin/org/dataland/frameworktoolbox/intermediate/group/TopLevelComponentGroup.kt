package org.dataland.frameworktoolbox.intermediate.group

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.TreeNode
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase

/**
 * The Top-Level component group is a component group that does not have a TreeNode as its parent
 * but rather the framework.
 */
class TopLevelComponentGroup(
    override val parent: Framework,
    private val componentGroupApi: ComponentGroupApiImpl = ComponentGroupApiImpl(),
) : TreeNode<Framework>,
    FieldNodeParent,
    ComponentGroupApi by componentGroupApi {
    override val children: Sequence<ComponentBase> by componentGroupApi::children

    init {
        componentGroupApi.parent = this
    }
}
