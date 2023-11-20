package org.dataland.datalandbackend.frameworks.heimathafen.model.social

import org.dataland.datalandbackend.frameworks.heimathafen.model.social.nachhaltigskeitsrisiken.HeimathafenSocialNachhaltigskeitsrisiken
import org.dataland.datalandbackend.frameworks.heimathafen.model.social.pais.HeimathafenSocialPais
import org.dataland.datalandbackend.frameworks.heimathafen.model.social.sfdr.HeimathafenSocialSfdr
import org.dataland.datalandbackend.frameworks.heimathafen.model.social.kontroverseGeschaeftsfelder.HeimathafenSocialKontroverseGeschaeftsfelder

/**
 * The data-model for the Social section
 */
data class HeimathafenSocial(
    val nachhaltigskeitsrisiken: HeimathafenSocialNachhaltigskeitsrisiken?,
    val pais: HeimathafenSocialPais?,
    val sfdr: HeimathafenSocialSfdr?,
    val kontroverseGeschaeftsfelder: HeimathafenSocialKontroverseGeschaeftsfelder?,
)
