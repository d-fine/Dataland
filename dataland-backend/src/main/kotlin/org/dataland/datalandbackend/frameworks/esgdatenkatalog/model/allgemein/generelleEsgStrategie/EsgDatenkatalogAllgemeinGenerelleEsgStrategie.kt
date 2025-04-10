// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.esgdatenkatalog.model.allgemein.generelleEsgStrategie

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.validator.MinimumValue
import java.math.BigDecimal

/**
 * The data-model for the GenerelleEsgStrategie section
 */
data class EsgDatenkatalogAllgemeinGenerelleEsgStrategie(
    val nachhaltigkeitsstrategieVorhanden: YesNo? = null,
    @field:Valid()
    val dokumenteZurNachhaltigkeitsstrategie: List<BaseDataPoint<String>>? = null,
    val massnahmenZurErreichungDes15GradCelsiusZiels: YesNo? = null,
    val skizzierungVonMassnahmenZurErreichungDes15GradCelsiusZiels: String? = null,
    @field:MinimumValue(minimumValue = 0)
    val zugewieseneBudgetsBis2030: BigDecimal? = null,
    @field:MinimumValue(minimumValue = 0)
    val zugewieseneBudgetsAb2031: BigDecimal? = null,
    @field:MinimumValue(minimumValue = 0)
    val erwarteterFinanzierungsbedarfBis2030: BigDecimal? = null,
    @field:MinimumValue(minimumValue = 0)
    val erwarteterFinanzierungsbedarfAb2031: BigDecimal? = null,
    @field:MinimumValue(minimumValue = 0)
    val geplanteVollzeitaequivalenteBis2023: BigDecimal? = null,
    @field:MinimumValue(minimumValue = 0)
    val geplanteVollzeitaequivalenteAb2031: BigDecimal? = null,
    val chancenOderHindernisse: String? = null,
)
