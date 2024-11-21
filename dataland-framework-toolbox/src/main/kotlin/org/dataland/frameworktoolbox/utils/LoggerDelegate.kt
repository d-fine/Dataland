package org.dataland.frameworktoolbox.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A LoggerDelegate is a comfortable way to get an SLF4J Logger for the hosting class
 */
class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, Logger> {
    override fun getValue(
        thisRef: R,
        property: KProperty<*>,
    ): Logger = LoggerFactory.getLogger(thisRef.javaClass)
}
