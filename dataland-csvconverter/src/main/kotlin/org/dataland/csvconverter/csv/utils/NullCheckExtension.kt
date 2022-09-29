package org.dataland.csvconverter.csv.utils

import kotlin.reflect.full.memberProperties

/**
 * An object providing the reflection-based checkifAllFieldsAreNull function
 */
object NullCheckExtension {
    /**
     * Checks if all memberProperties of a kotlin class are null. Useful to move the null to the highest level
     * if e.g. all properties of the InvestmentFirmKpis are null, the entire object should just be set to null
     */
    fun Any.checkIfAllFieldsAreNull(): Boolean {
        for (f in javaClass.kotlin.memberProperties) if (f.get(this) != null) return false
        return true
    }
}
