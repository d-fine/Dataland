package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.generics.Address

/**
 * Base class for a Production Site in frameworks
 */
interface ProductionSiteInterface {
    val nameOfProductionSite: String?
    val addressOfProductionSite: Address
}
