// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.esgdatenkatalog.model.soziales.risikenUndMassnahmen

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the RisikenUndMassnahmen section
 */
data class EsgDatenkatalogSozialesRisikenUndMassnahmen(
    val weitereWesentlicheSozialeRisiken: YesNo? = null,
    val erlaeuterungZuWeiterenWesentlichenSozialenRisiken: String? = null,
    val massnahmenZurReduzierungVonSozialenRisiken: String? = null,
)
