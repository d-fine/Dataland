package org.dataland.frameworktoolbox.utils

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** A LoggerDelegate is a comfortable way to get an SLF4J Logger for the hosting class */
class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, Logger> {
  override fun getValue(thisRef: R, property: KProperty<*>): Logger {
    return LoggerFactory.getLogger(thisRef.javaClass)
  }
}
