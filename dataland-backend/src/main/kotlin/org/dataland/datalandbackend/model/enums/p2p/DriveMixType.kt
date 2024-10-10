package org.dataland.datalandbackend.model.enums.p2p

import io.swagger.v3.oas.annotations.media.Schema

/**
 * A drive mix type
 */
@Schema(
    enumAsRef = true,
)
enum class DriveMixType {
    SmallTrucks,
    MediumTrucks,
    LargeTrucks,
}
