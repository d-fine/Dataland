package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import kotlin.reflect.KClass
import jakarta.validation.Payload

@Constraint(validatedBy = [SfdrDataValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ReferencedReportsListIsComplete(
    val message: String = "There are company reports that are ",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)