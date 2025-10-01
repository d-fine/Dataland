package org.dataland.datalandbackend.exceptions

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException

/**
 * An InvalidReportingPeriodShiftException should be thrown if the user enters a value that is not 0 or -1.
 */
class InvalidReportingPeriodShiftException :
    InvalidInputApiException(
        summary = "Invalid reporting period shift",
        message = "Only 0 (no deviation) or -1 (deviation) are allowed.",
        cause = null,
    )
