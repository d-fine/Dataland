package org.dataland.datalandbackend.model

/**
 * An interface describing common properties of all frameworks
 */
interface FrameworkBase {
    val referencedReports: Map<String, CompanyReport>?
}
