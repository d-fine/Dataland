package org.dataland.datalanduserservice.model.enums

import io.swagger.v3.oas.annotations.media.Schema

/**
 * A class that holds the frequencies notifications can be sent
 */
@Schema(
    enumAsRef = true,
)
enum class NotificationFrequency {
    NoNotification,
    Daily,
    Weekly,
    Monthly,
}
