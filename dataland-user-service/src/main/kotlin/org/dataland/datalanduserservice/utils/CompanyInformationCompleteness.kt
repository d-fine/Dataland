package org.dataland.datalanduserservice.utils

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation

/**
 * Returns true if the company lacks a fiscal year end or a sector (null or blank).
 */
fun CompanyInformation.isMissingFiscalYearEndOrSector(): Boolean = fiscalYearEnd.isNullOrBlank() || sector.isNullOrBlank()
