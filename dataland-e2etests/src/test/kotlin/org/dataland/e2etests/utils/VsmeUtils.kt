package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataVsmeData
import org.dataland.datalandbackend.openApiClient.model.CompanyReport
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.VsmeData
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.customApiControllers.CustomVsmeDataControllerApi
import org.dataland.e2etests.tests.frameworks.Vsme.FileInfos
import java.io.File
import java.time.LocalDate

class VsmeUtils {

    val apiAccessor = ApiAccessor()

    fun postVsmeDataset(
        companyAssociatedDataVsmeData: CompanyAssociatedDataVsmeData,
        documents: List<File> = listOf(),
        user: TechnicalUser,
    ): DataMetaInformation {
        val keycloakToken = apiAccessor.jwtHelper.obtainJwtForTechnicalUser(user)
        val customVsmeDataControllerApi = CustomVsmeDataControllerApi(keycloakToken)
        return customVsmeDataControllerApi.postCompanyAssociatedDataVsmeData(
            companyAssociatedDataVsmeData,
            documents,
        )
    }

    fun setReferencedReports(dataset: VsmeData, fileInfoToSetAsReport: FileInfos?): VsmeData {
        val newReferencedReports = fileInfoToSetAsReport?.let {
            mapOf(
                it.fileName to CompanyReport(
                    fileReference = it.fileReference,
                    fileName = it.fileName,
                    publicationDate = LocalDate.now(),
                ),
            )
        }
        return dataset.copy(
            basic = dataset.basic?.copy(
                basisForPreparation = dataset.basic?.basisForPreparation?.copy(
                    referencedReports = newReferencedReports,
                ),
            ),
        )
    }
}
