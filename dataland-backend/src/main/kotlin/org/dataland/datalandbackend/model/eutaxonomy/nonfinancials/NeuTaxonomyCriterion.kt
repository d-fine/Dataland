package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

// TODO also long names are provided for each objective. Also add these as values?
// TODO rename this to "NeuTaxonomyObjective"?
enum class NeuTaxonomyCriterion(val jsonName: String) {
    ClimateMitigation("mitigation"),
    ClimateAdaption("adaption"),
    Water("water"),
    CircularEconomy("circular"),
    PollutionPrevention("pollution"),
    Biodiversity("biodiversity"),
}
