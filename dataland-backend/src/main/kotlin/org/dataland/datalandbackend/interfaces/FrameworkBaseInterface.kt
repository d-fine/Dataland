package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.CompanyReport

/**
 * An interface describing common properties of all frameworks
 */
interface FrameworkBaseInterface {
    val referencedReports: Map<String, CompanyReport>?
}
