package org.dataland.datalandbackend.frameworks.gdv.model.umwelt.biodiversitaet

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the Biodiversitaet section
 */
data class GdvUmweltBiodiversitaet(
    val negativeAktivitaetenFuerDieBiologischeVielfalt: YesNo?,
    val negativeMassnahmenFuerDieBiologischeVielfalt: String?,
    val positiveAktivitaetenFuerDieBiologischeVielfalt: YesNo?,
    val positiveMassnahmenFuerDieBiologischeVielfalt: String?,
)
