package org.dataland.datalandcommunitymanager.model.dataRequest

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * --- API model ---
 * Contains all necessary info that a user has to provide in order to request a single datasets on Dataland.
 * @param companyId contains the dataland company identifier for which the user wants to request framework data
 * @param selectedFramework contains the names of the framework, for which the user wants to request framework data
 * @param yearsRequested contains the years for which the user requests data
 * @param email contains the email provided by the user for contacting
 * @param message contains the message provided by the user for further context
 */
data class SingleDataRequest(
    val companyId: String,
    val selectedFramework: DataTypeEnum,
    val yearsRequested: List<Number>,
    val email: String?,
    val message: String?,
)
