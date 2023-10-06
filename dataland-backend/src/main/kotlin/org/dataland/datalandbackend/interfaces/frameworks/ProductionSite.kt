package org.dataland.datalandbackend.interfaces.frameworks

import org.dataland.datalandbackend.model.generics.Address

/**
 * Interface for a basic Production Site in frameworks
 */
interface ProductionSite {
    val nameOfProductionSite: String?
    val addressOfProductionSite: Address
}
