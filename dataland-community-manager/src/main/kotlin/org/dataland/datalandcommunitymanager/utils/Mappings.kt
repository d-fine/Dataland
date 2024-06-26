package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

val readableFrameworkNameMapping: Map<DataTypeEnum, String> = mapOf(
    DataTypeEnum.eutaxonomyMinusFinancials to "EU Taxonomy for financial companies",
    DataTypeEnum.eutaxonomyMinusNonMinusFinancials to "EU Taxonomy for non-financial companies",
    DataTypeEnum.lksg to "LkSG",
    DataTypeEnum.sfdr to "SFDR",
    DataTypeEnum.vsme to "VSME",
    DataTypeEnum.p2p to "WWF Pathway to Paris",
    DataTypeEnum.esgMinusQuestionnaire to "ESG Questionnaire",
    DataTypeEnum.heimathafen to "Heimathafen",
)
