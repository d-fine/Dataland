package org.dataland.datalandbackend.frameworks.heimathafen.model.environmental

import org.dataland.datalandbackend.frameworks.heimathafen.model.environmental.kontroverseGeschaeftsfelder.HeimathafenEnvironmentalKontroverseGeschaeftsfelder
import org.dataland.datalandbackend.frameworks.heimathafen.model.environmental.nachhaltigskeitsrisiken.HeimathafenEnvironmentalNachhaltigskeitsrisiken
import org.dataland.datalandbackend.frameworks.heimathafen.model.environmental.pais.HeimathafenEnvironmentalPais
import org.dataland.datalandbackend.frameworks.heimathafen.model.environmental.sfdr.HeimathafenEnvironmentalSfdr

/**
 * The data-model for the Environmental section
 */
data class HeimathafenEnvironmental(
    val nachhaltigskeitsrisiken: HeimathafenEnvironmentalNachhaltigskeitsrisiken?,
    val pais: HeimathafenEnvironmentalPais?,
    val sfdr: HeimathafenEnvironmentalSfdr?,
    val kontroverseGeschaeftsfelder: HeimathafenEnvironmentalKontroverseGeschaeftsfelder?,
)
