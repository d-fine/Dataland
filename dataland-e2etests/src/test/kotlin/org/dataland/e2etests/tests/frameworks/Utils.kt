package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler

fun <T, U> postOneCompanyAndItsData(
    testDataProvider: TestDataProvider<T>,
    postCompanyAssociatedData: (U) -> DataMetaInformation,
    myConstructor: (String, T) -> U
):
    Pair<DataMetaInformation, T> {
    TokenHandler().obtainTokenForUserType(TokenHandler.UserType.Uploader)
    val testData = testDataProvider.getTData(1).first()
    val receivedCompanyId = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND).postCompany(
        testDataProvider.getCompanyInformationWithoutIdentifiers(1).first()
    ).companyId
    val receivedDataMetaInformation = postCompanyAssociatedData(myConstructor(receivedCompanyId, testData))
    return Pair(
        DataMetaInformation(
            companyId = receivedCompanyId,
            dataId = receivedDataMetaInformation.dataId,
            dataType = receivedDataMetaInformation.dataType
        ),
        testData
    )
}
