package org.dataland.csvconverter.csv.utils

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * This class only passes the map for the YesNoNa Enum to the EnumCsv
 * in order to use it in several locations.
 */
class YesNoNaParser : EnumCsvParser<YesNoNa>(
    mapOf(
        "Yes" to YesNoNa.Yes,
        "No" to YesNoNa.No,
        "N/A" to YesNoNa.NA,
    ),
)

/**
 * This class only passes the map for the YesNo Enum to the EnumCsv
 * in order to use it in several locations.
 */
class YesNoParser : EnumCsvParser<YesNo>(
    mapOf(
        "Yes" to YesNo.Yes,
        "No" to YesNo.No,
    ),
)
