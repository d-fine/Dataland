package org.dataland.datalandbackend.model.generics

/**
 * Base class for a Production Site in frameworks
 */
open class ProductionSiteBase(
    open val nameOfProductionSite: String?,

    open val addressOfProductionSite: Address,
)
