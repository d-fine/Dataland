package org.dataland.datalandbackend.controller

/**
 * Controller for the LkSG framework endpoints
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
/**
class LksgDataController(
 @Autowired var myDataManager: DataManager,
 @Autowired var myMetaDataManager: DataMetaInformationManager,
 @Autowired var myObjectMapper: ObjectMapper,
) : DataController<LksgData>(
 myDataManager,
 myMetaDataManager,
 myObjectMapper,
 LksgData::class.java,
) {
 //@Operation(operationId = "getCompanyAssociatedLksgData")
 override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<LksgData>> {
 return super.getCompanyAssociatedData(dataId)
 }

 //@Operation(operationId = "postCompanyAssociatedLksgData")
 override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<LksgData>, bypassQa: Boolean):
 ResponseEntity<DataMetaInformation> {
 return super.postCompanyAssociatedData(companyAssociatedData, bypassQa)
 }

 //@Operation(operationId = "getAllCompanyLksgData")
 override fun getFrameworkDatasetsForCompany(
 companyId: String,
 showOnlyActive: Boolean,
 reportingPeriod: String?,
 ): ResponseEntity<List<DataAndMetaInformation<LksgData>>> {
 return super.getFrameworkDatasetsForCompany(companyId, showOnlyActive, reportingPeriod)
 }
}
*/
