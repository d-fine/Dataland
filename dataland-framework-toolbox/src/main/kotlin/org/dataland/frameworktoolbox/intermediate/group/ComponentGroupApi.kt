package org.dataland.frameworktoolbox.intermediate.group

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import kotlin.reflect.KClass

/**
 * A hierarchical group of subelements with identifiers. Allows users to manipulate (CRUD) subelements
 * using their identifier. Uses Kotlin's Type-Safe-Builders to allow for DSL-Like intuitive manipulation of
 * Elements
 */
interface ComponentGroupApi {
    /**
     * Create a new subcomponent. Throws an exception when the identifier is already in use
     * or the provided class cannot be constructed.
     */
    fun <T : ComponentBase> create(
        identifier: String,
        insertBeforeIdentifier: String? = null,
        clazz: KClass<T>,
        init: (T.() -> Unit)? = null,
    ): T

    /**
     * Edit an existing subcomponent. Throws an exception when the identifier is not existent or
     * the identified element has a different type than specified
     */
    fun <T : ComponentBase> edit(
        identifier: String,
        clazz: KClass<T>,
        editFunction: T.() -> Unit,
    ): T

    /**
     * Get an existing subcomponent or null. Throws an exception when the identified element has a different type
     * than specified
     */
    fun <T : ComponentBase> getOrNull(
        identifier: String,
        clazz: KClass<T>,
    ): T?

    /**
     * Get an existing subcomponent. Throws an exception when the identified element has a different type
     * than specified or does not exist.
     */
    fun <T : ComponentBase> get(
        identifier: String,
        clazz: KClass<T>,
    ): T

    /**
     * Delete an existing subcomponent. Throws an exception when the identifier is not existent or
     * the identified element has a different type than specified.
     */
    fun <T : ComponentBase> delete(
        identifier: String,
        clazz: KClass<T>,
    )
}

/**
 * Create a new subcomponent. Throws an exception when the identifier is already in use
 * or the provided class cannot be constructed.
 */
inline fun <reified T : ComponentBase> ComponentGroupApi.create(
    identifier: String,
    insertBeforeIdentifier: String? = null,
    noinline init: (T.() -> Unit)? = null,
): T = this.create(identifier, insertBeforeIdentifier, T::class, init)

/**
 * Edit an existing subcomponent. Throws an exception when the identifier is not existent or
 * the identified element has a different type than specified
 */
inline fun <reified T : ComponentBase> ComponentGroupApi.edit(
    identifier: String,
    noinline editFunction: T.() -> Unit,
): T = this.edit(identifier, T::class, editFunction)

/**
 * Delete an existing subcomponent. Throws an exception when the identifier is not existent or
 * the identified element has a different type than specified.
 */
inline fun <reified T : ComponentBase> ComponentGroupApi.delete(identifier: String) = this.delete(identifier, T::class)

/**
 * Get an existing subcomponent or null. Throws an exception when the identified element has a different type
 * than specified
 */
inline fun <reified T : ComponentBase> ComponentGroupApi.getOrNull(identifier: String): T? = this.getOrNull(identifier, T::class)

/**
 * Get an existing subcomponent. Throws an exception when the identified element has a different type
 * than specified or does not exist.getOrNull
 */
inline fun <reified T : ComponentBase> ComponentGroupApi.get(identifier: String): T = this.get(identifier, T::class)
