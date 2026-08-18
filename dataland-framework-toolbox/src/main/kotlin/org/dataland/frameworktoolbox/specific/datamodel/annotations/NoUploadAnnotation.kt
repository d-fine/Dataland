package org.dataland.frameworktoolbox.specific.datamodel.annotations

import org.dataland.frameworktoolbox.specific.datamodel.Annotation

/**
 * Validating Annotation for specifying that a DataPoint is not to be uploaded
 */
class NoUploadAnnotation :
    Annotation(
        "org.dataland.datalandbackend.validator.NoUpload",
        applicationTargetPrefix = "field",
    )
