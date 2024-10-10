package org.dataland.datalandbackend.annotations

/**
 * Data type annotation
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DataType(
    val name: String,
    val order: Int,
)
