// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt

import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt.abfallproduktion
    .EsgQuestionnaireUmweltAbfallproduktion
import org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt.biodiversitaet
    .EsgQuestionnaireUmweltBiodiversitaet
import org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt.energieeffizienzImmobilienanlagen
    .EsgQuestionnaireUmweltEnergieeffizienzImmobilienanlagen
import org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt.energieverbrauch
    .EsgQuestionnaireUmweltEnergieverbrauch
import org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt.fossileBrennstoffe
    .EsgQuestionnaireUmweltFossileBrennstoffe
import org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt.produktion.EsgQuestionnaireUmweltProduktion
import org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt.taxonomie.EsgQuestionnaireUmweltTaxonomie
import org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt.treibhausgasemissionen
    .EsgQuestionnaireUmweltTreibhausgasemissionen
import org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt.wasserverbrauch
    .EsgQuestionnaireUmweltWasserverbrauch

/**
 * The data-model for the Umwelt section
 */
data class EsgQuestionnaireUmwelt(
    @field:Valid()
    val treibhausgasemissionen: EsgQuestionnaireUmweltTreibhausgasemissionen? = null,
    @field:Valid()
    val produktion: EsgQuestionnaireUmweltProduktion? = null,
    @field:Valid()
    val energieverbrauch: EsgQuestionnaireUmweltEnergieverbrauch? = null,
    @field:Valid()
    val energieeffizienzImmobilienanlagen: EsgQuestionnaireUmweltEnergieeffizienzImmobilienanlagen? = null,
    @field:Valid()
    val wasserverbrauch: EsgQuestionnaireUmweltWasserverbrauch? = null,
    @field:Valid()
    val abfallproduktion: EsgQuestionnaireUmweltAbfallproduktion? = null,
    @field:Valid()
    val biodiversitaet: EsgQuestionnaireUmweltBiodiversitaet? = null,
    @field:Valid()
    val fossileBrennstoffe: EsgQuestionnaireUmweltFossileBrennstoffe? = null,
    @field:Valid()
    val taxonomie: EsgQuestionnaireUmweltTaxonomie? = null,
)
