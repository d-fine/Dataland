package org.dataland.frameworktoolbox.specific.datamodel.annotations

import org.dataland.frameworktoolbox.specific.datamodel.Annotation

/**
 * Validating Annotation for specifying a minimum value of numerical DataPoint
 */
class DataPointMinimumValueAnnotation(minimumValue: Long) : Annotation(
    "org.dataland.datalandbackend.validator.DataPointMinimumValue",
    applicationTargetPrefix = "field",
    rawParameterSpec = "minimumValue = $minimumValue",
)
