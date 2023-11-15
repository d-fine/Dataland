package org.dataland.datalandbackend.interfaces.frameworks

import org.dataland.datalandbackend.model.documents.CompanyReport

/**
 * An interface describing common properties of all frameworks
 */
interface FrameworkBase {
    val referencedReports: Map<String, CompanyReport>?
}
