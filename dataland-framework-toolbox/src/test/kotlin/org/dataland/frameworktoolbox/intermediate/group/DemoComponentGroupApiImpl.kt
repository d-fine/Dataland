package org.dataland.frameworktoolbox.intermediate.group

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase

class DemoComponentGroupApiImpl(
    private val componentGroupApi: ComponentGroupApiImpl = ComponentGroupApiImpl(),
) : ComponentGroupApi by componentGroupApi,
    FieldNodeParent {
    override val children: Sequence<ComponentBase>
        get() = componentGroupApi.children

    init {
        componentGroupApi.parent = this
    }
}
