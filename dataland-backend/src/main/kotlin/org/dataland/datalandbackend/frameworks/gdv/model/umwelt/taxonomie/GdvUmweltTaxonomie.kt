package org.dataland.datalandbackend.frameworks.gdv.model.umwelt.taxonomie

/**
 * The data-model for the Taxonomie section
 */
data class GdvUmweltTaxonomie(
    val taxonomieBerichterstattung: TaxonomieBerichterstattungOptions?,
    val euTaxonomieKompassAktivitaeten: List<String>?,
)
