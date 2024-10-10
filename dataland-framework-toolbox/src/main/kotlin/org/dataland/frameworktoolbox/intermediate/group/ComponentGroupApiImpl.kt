package org.dataland.frameworktoolbox.intermediate.group

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import kotlin.reflect.KClass

/**
 * A generic implementation of the ComponentGroupApi. This class is intended to
 * provide the backbone for components that require dynamic subcomponents
 * (e.g., Sections or Lists of Objects)
 */
class ComponentGroupApiImpl(
    var parent: FieldNodeParent? = null,
) : ComponentGroupApi {
    private val components: MutableList<ComponentBase> = mutableListOf()

    val children: Sequence<ComponentBase>
        get() = components.asSequence()

    private fun <T : ComponentBase> getComponentWithIdentifierAndType(
        identifier: String,
        clazz: KClass<T>,
    ): T =
        getOrNull(identifier, clazz)
            ?: throw IllegalArgumentException("Could not find the component with identifier $identifier.")

    private fun <T : ComponentBase> initComponent(
        component: T,
        insertBeforeIdentifier: String? = null,
        init: (T.() -> Unit)?,
    ): T {
        require(components.none { it.identifier == component.identifier }) {
            "The identifier ${component.identifier} already exists."
        }
        init?.let { component.init() }
        if (insertBeforeIdentifier == null) {
            components.add(component)
        } else {
            val indexOfInsertBeforeIdentifier = components.indexOfFirst { it.identifier == insertBeforeIdentifier }

            require(indexOfInsertBeforeIdentifier != -1) {
                "A component for the field ${component.identifier} cannot be added before a component for a field " +
                    "$insertBeforeIdentifier because the component group does not contain a field with the name " +
                    "$insertBeforeIdentifier."
            }
            components.add(indexOfInsertBeforeIdentifier, component)
        }
        return component
    }

    /**
     * Searches for a suitable constructor of the provided class.
     * A constructor is suitable if it has two required arguments
     * (1: Identifier as a string, 2: Parent as a FieldNodeParent)
     */
    private fun <T : ComponentBase> getComponentConstructorByReflection(clazz: KClass<T>): ((String, FieldNodeParent) -> T)? {
        val componentConstructors = clazz.constructors
        for (constructor in componentConstructors) {
            val nonOptimalParameters = constructor.parameters.filter { !it.isOptional }
            val validConstructor =
                nonOptimalParameters.size == 2 &&
                    nonOptimalParameters[0].type.classifier == String::class &&
                    nonOptimalParameters[1].type.classifier == FieldNodeParent::class
            if (validConstructor) {
                return { identifier, parent ->
                    constructor.callBy(mapOf(nonOptimalParameters[0] to identifier, nonOptimalParameters[1] to parent))
                }
            }
        }
        return null
    }

    override fun <T : ComponentBase> create(
        identifier: String,
        insertBeforeIdentifier: String?,
        clazz: KClass<T>,
        init: (T.() -> Unit)?,
    ): T {
        val localParent = parent
        requireNotNull(localParent) { "Cannot initialize a new component without an initialized parent." }

        val componentConstructor = getComponentConstructorByReflection(clazz)
        requireNotNull(componentConstructor) { "Could not find any suitable constructor for creating $clazz" }

        val component = componentConstructor(identifier, localParent)
        return initComponent(component, insertBeforeIdentifier, init)
    }

    override fun <T : ComponentBase> edit(
        identifier: String,
        clazz: KClass<T>,
        editFunction: T.() -> Unit,
    ): T {
        val component = getComponentWithIdentifierAndType(identifier, clazz)
        editFunction(component)
        return component
    }

    override fun <T : ComponentBase> get(
        identifier: String,
        clazz: KClass<T>,
    ): T = getComponentWithIdentifierAndType(identifier, clazz)

    override fun <T : ComponentBase> getOrNull(
        identifier: String,
        clazz: KClass<T>,
    ): T? {
        val componentWithIdentifier = components.find { it.identifier == identifier } ?: return null

        require(clazz.isInstance(componentWithIdentifier)) {
            "The component with identifier $identifier is of type ${componentWithIdentifier::class}. Expected $clazz."
        }

        @Suppress("UNCHECKED_CAST")
        return componentWithIdentifier as T
    }

    override fun <T : ComponentBase> delete(
        identifier: String,
        clazz: KClass<T>,
    ) {
        val component = getComponentWithIdentifierAndType(identifier, clazz)
        components.remove(component)
    }
}
