package org.dataland.frameworktoolbox.specific.datamodel.annotations

import org.dataland.frameworktoolbox.specific.datamodel.Annotation

/**
 * Validating Annotation for specifying a minimum value of numerical DataPoint
 */
class MinimumValueAnnotation(
    minimumValue: Long,
) : Annotation(
        "org.dataland.datalandbackend.validator.MinimumValue",
        applicationTargetPrefix = "field",
        rawParameterSpec = "minimumValue = $minimumValue",
    )
