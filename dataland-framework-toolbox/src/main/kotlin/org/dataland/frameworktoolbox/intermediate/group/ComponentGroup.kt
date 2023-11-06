package org.dataland.frameworktoolbox.intermediate.group

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.utils.capitalizeEn

/**
 * A collection of components (i.e., a section or subsection).
 */
class ComponentGroup(
    identifier: String,
    parent: FieldNodeParent,
    private val componentGroupApi: ComponentGroupApiImpl = ComponentGroupApiImpl(),
) : ComponentBase(identifier, parent), FieldNodeParent, ComponentGroupApi by componentGroupApi {

    override val children: Sequence<ComponentBase> by componentGroupApi::children

    val camelCaseComponentIdentifier: String
        get() {
            return parents()
                .toList()
                .reversed()
                .mapNotNull {
                    when (it) {
                        is ComponentGroup -> it.identifier.capitalizeEn()
                        is TopLevelComponentGroup -> it.parent.identifier.capitalizeEn()
                        else -> null
                    }
                }.joinToString("") + identifier.capitalizeEn()
        }

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val groupPackage = dataClassBuilder.parentPackage.addPackage(identifier)
        val groupClass = groupPackage.addClass(
            camelCaseComponentIdentifier,
            "The data-model for the ${identifier.capitalizeEn()} section",
        )

        children.forEach {
            it.generateDataModel(groupClass)
        }

        dataClassBuilder.addProperty(
            identifier,
            groupClass.getTypeReference(nullable = isNullable),
        )
    }

    init {
        componentGroupApi.parent = this
    }
}
