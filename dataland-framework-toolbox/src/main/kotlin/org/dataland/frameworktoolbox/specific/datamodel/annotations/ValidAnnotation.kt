package org.dataland.frameworktoolbox.specific.datamodel.annotations

import org.dataland.frameworktoolbox.specific.datamodel.Annotation

/**
 * Represents the Jakarta "Valid" annotation required for recursive spring field validation
 */
object ValidAnnotation : Annotation(
    fullyQualifiedName = "jakarta.validation.Valid",
    applicationTargetPrefix = "field",
)
