package org.dataland.datalandbackend.frameworks.gdv.model.soziales.audit

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.frameworks.gdv.model.soziales.audit.ArtDesAuditsOptions

/**
 * The data-model for the Audit section
 */
data class GdvSozialesAudit(
    val auditsZurEinhaltungVonArbeitsstandards: YesNo?,
    val artDesAudits: ArtDesAuditsOptions?,
    val auditErgebnisse: String?,
)
