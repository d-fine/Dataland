package org.dataland.frameworktoolbox.specific.specification

import org.dataland.frameworktoolbox.intermediate.datapoints.DocumentSupport
import org.dataland.frameworktoolbox.utils.capitalizeEn

object SpecificationNamingConvention {
    fun generateName(
        documentSupport: DocumentSupport,
        identifier: String,
        dataType: String,
    ): String {
        val documentSupportPrefix = documentSupport.getNamingPrefix()

        return "${documentSupportPrefix}${dataType.capitalizeEn()}${identifier.capitalizeEn()}"
    }
}
