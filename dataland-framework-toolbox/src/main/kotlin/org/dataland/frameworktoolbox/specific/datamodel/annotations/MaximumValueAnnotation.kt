package org.dataland.frameworktoolbox.specific.datamodel.annotations

import org.dataland.frameworktoolbox.specific.datamodel.Annotation

/**
 * Validating Annotation for specifying a minimum value of numerical DataPoint
 */
class MaximumValueAnnotation(
    maximumValue: Long,
) : Annotation(
        "org.dataland.datalandbackend.validator.MaximumValue",
        applicationTargetPrefix = "field",
        rawParameterSpec = "maximumValue = $maximumValue",
    )
