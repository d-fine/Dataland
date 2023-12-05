package org.dataland.datalandbackend.frameworks.gdv.model.umwelt.taxonomie

import org.dataland.datalandbackend.frameworks.gdv.model.umwelt.taxonomie.TaxonomieBerichterstattungOptions

/**
 * The data-model for the Taxonomie section
 */
data class GdvUmweltTaxonomie(
    val taxonomieBerichterstattung: TaxonomieBerichterstattungOptions?,
    val euTaxonomieKompassAktivitaeten: List<String>?,
)
