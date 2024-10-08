// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.heimathafen.model.general

import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleBezahlbareUndSaubereEnergie
    .HeimathafenGeneralImpactmerkmaleBezahlbareUndSaubereEnergie
import org.dataland.datalandbackend.frameworks.heimathafen.model.general
    .impactmerkmaleFriedenGerechtigkeitUndStarkeInstitutionen
    .HeimathafenGeneralImpactmerkmaleFriedenGerechtigkeitUndStarkeInstitutionen
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleGeschlechtergleichheit
    .HeimathafenGeneralImpactmerkmaleGeschlechtergleichheit
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleGesundheitUndWohlergehen
    .HeimathafenGeneralImpactmerkmaleGesundheitUndWohlergehen
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleHochwertigeBildung
    .HeimathafenGeneralImpactmerkmaleHochwertigeBildung
import org.dataland.datalandbackend.frameworks.heimathafen.model.general
    .impactmerkmaleIndustrieInnovationUndInfrastruktur
    .HeimathafenGeneralImpactmerkmaleIndustrieInnovationUndInfrastruktur
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleKeinHunger
    .HeimathafenGeneralImpactmerkmaleKeinHunger
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleKeineArmut
    .HeimathafenGeneralImpactmerkmaleKeineArmut
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleLebenAndLand
    .HeimathafenGeneralImpactmerkmaleLebenAndLand
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleLebenUnterWasser
    .HeimathafenGeneralImpactmerkmaleLebenUnterWasser
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleMassnahmenZumKlimaschutz
    .HeimathafenGeneralImpactmerkmaleMassnahmenZumKlimaschutz
import org.dataland.datalandbackend.frameworks.heimathafen.model.general
    .impactmerkmaleMenschenwuerdigeArbeitUndWirtschaftswachstum
    .HeimathafenGeneralImpactmerkmaleMenschenwuerdigeArbeitUndWirtschaftswachstum
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleNachhaltigeStaedteUndGemeinden
    .HeimathafenGeneralImpactmerkmaleNachhaltigeStaedteUndGemeinden
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleNachhaltigerKonsumUndProduktion
    .HeimathafenGeneralImpactmerkmaleNachhaltigerKonsumUndProduktion
import org.dataland.datalandbackend.frameworks.heimathafen.model.general
    .impactmerkmalePartnerschaftenZurErreichungDerZiele
    .HeimathafenGeneralImpactmerkmalePartnerschaftenZurErreichungDerZiele
import org.dataland.datalandbackend.frameworks.heimathafen.model.general
    .impactmerkmaleSauberesWasserUndSanitaereEinrichtungen
    .HeimathafenGeneralImpactmerkmaleSauberesWasserUndSanitaereEinrichtungen
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmaleWenigerUngleichheiten
    .HeimathafenGeneralImpactmerkmaleWenigerUngleichheiten
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.implementierung
    .HeimathafenGeneralImplementierung
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.methodik.HeimathafenGeneralMethodik
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.unternehmen.HeimathafenGeneralUnternehmen

/**
 * The data-model for the General section
 */
data class HeimathafenGeneral(
    @field:Valid()
    val unternehmen: HeimathafenGeneralUnternehmen? = null,
    @field:Valid()
    val methodik: HeimathafenGeneralMethodik? = null,
    @field:Valid()
    val impactmerkmaleKeineArmut: HeimathafenGeneralImpactmerkmaleKeineArmut? = null,
    @field:Valid()
    val impactmerkmaleKeinHunger: HeimathafenGeneralImpactmerkmaleKeinHunger? = null,
    @field:Valid()
    val impactmerkmaleGesundheitUndWohlergehen: HeimathafenGeneralImpactmerkmaleGesundheitUndWohlergehen? = null,
    @field:Valid()
    val impactmerkmaleHochwertigeBildung: HeimathafenGeneralImpactmerkmaleHochwertigeBildung? = null,
    @field:Valid()
    val impactmerkmaleGeschlechtergleichheit: HeimathafenGeneralImpactmerkmaleGeschlechtergleichheit? = null,
    @field:Valid()
    val impactmerkmaleSauberesWasserUndSanitaereEinrichtungen:
        HeimathafenGeneralImpactmerkmaleSauberesWasserUndSanitaereEinrichtungen? = null,
    @field:Valid()
    val impactmerkmaleBezahlbareUndSaubereEnergie: HeimathafenGeneralImpactmerkmaleBezahlbareUndSaubereEnergie? = null,
    @field:Valid()
    val impactmerkmaleMenschenwuerdigeArbeitUndWirtschaftswachstum:
        HeimathafenGeneralImpactmerkmaleMenschenwuerdigeArbeitUndWirtschaftswachstum? = null,
    @field:Valid()
    val impactmerkmaleIndustrieInnovationUndInfrastruktur: HeimathafenGeneralImpactmerkmaleIndustrieInnovationUndInfrastruktur? = null,
    @field:Valid()
    val impactmerkmaleWenigerUngleichheiten: HeimathafenGeneralImpactmerkmaleWenigerUngleichheiten? = null,
    @field:Valid()
    val impactmerkmaleNachhaltigeStaedteUndGemeinden: HeimathafenGeneralImpactmerkmaleNachhaltigeStaedteUndGemeinden? = null,
    @field:Valid()
    val impactmerkmaleNachhaltigerKonsumUndProduktion: HeimathafenGeneralImpactmerkmaleNachhaltigerKonsumUndProduktion? = null,
    @field:Valid()
    val impactmerkmaleMassnahmenZumKlimaschutz: HeimathafenGeneralImpactmerkmaleMassnahmenZumKlimaschutz? = null,
    @field:Valid()
    val impactmerkmaleLebenUnterWasser: HeimathafenGeneralImpactmerkmaleLebenUnterWasser? = null,
    @field:Valid()
    val impactmerkmaleLebenAndLand: HeimathafenGeneralImpactmerkmaleLebenAndLand? = null,
    @field:Valid()
    val impactmerkmaleFriedenGerechtigkeitUndStarkeInstitutionen:
        HeimathafenGeneralImpactmerkmaleFriedenGerechtigkeitUndStarkeInstitutionen? = null,
    @field:Valid()
    val impactmerkmalePartnerschaftenZurErreichungDerZiele: HeimathafenGeneralImpactmerkmalePartnerschaftenZurErreichungDerZiele? = null,
    @field:Valid()
    val implementierung: HeimathafenGeneralImplementierung? = null,
)
