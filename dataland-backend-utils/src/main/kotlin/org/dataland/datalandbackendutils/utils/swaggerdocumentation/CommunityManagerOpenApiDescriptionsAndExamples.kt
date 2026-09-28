package org.dataland.datalandbackendutils.utils.swaggerdocumentation

object CommunityManagerOpenApiDescriptionsAndExamples {
    const val COMPANY_ROLE_USER_ID_DESCRIPTION = "The unique identifier of the user in the company role assignment."
    const val COMPANY_ROLE_USER_EMAIL_ADDRESS_DESCRIPTION = "The email address of the user in the company role assignment."

    const val GENERAL_FIRST_NAME_DESCRIPTION = "The first name of the Dataland user in question."
    const val FIRST_NAME_EXAMPLE = "Jane"

    const val GENERAL_LAST_NAME_DESCRIPTION = "The last name of the Dataland user in question."
    const val LAST_NAME_EXAMPLE = "Doe"

    const val COMPANY_ROLE_FIRST_NAME_DESCRIPTION = "The first name of the user in the company role assignment."

    const val COMPANY_ROLE_LAST_NAME_DESCRIPTION = "The last name of the user in the company role assignment."

    const val DATA_REQUEST_ID_DESCRIPTION = "The unique identifier of the data request on Dataland."
    const val DATA_REQUEST_ID_EXAMPLE = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE

    const val COMPANY_ROLE_DESCRIPTION =
        "One of the Dataland specific roles which a user can have with respect to a company on Dataland."

    const val OWNERSHIP_COMMENT_DESCRIPTION = "An accompanying comment to a company ownership request."
    const val OWNERSHIP_COMMENT_EXAMPLE = "I am the CEO of this company, please make me its owner on Dataland."

    const val COMPANY_RIGHT_DESCRIPTION = "One of the Dataland-specific rights which can be assigned to a company on Dataland."

    const val INHERITED_ROLES_MAP_DESCRIPTION =
        "A map from Dataland company IDs to the list of inherited roles in those companies for the specified user."
    const val INHERITED_ROLES_MAP_EXAMPLE =
        "{\"${GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE}\":[\"DatalandMember\"]}"
}
