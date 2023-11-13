import { type AxiosPromise, type AxiosRequestConfig } from "axios";
import { type CompanyAssociatedData } from "@/api-models/CompanyAssociatedData";
import {
  type Configuration,
  type DataMetaInformation,
  type ${frameworkIdentifier?cap_first}Data,
  ${frameworkIdentifier?cap_first}DataControllerApi,
} from "@clients/backend";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";

export class ${frameworkIdentifier?cap_first}ApiClient implements FrameworkDataApi<${frameworkIdentifier?cap_first}Data> {
  private readonly openApiDataController: ${frameworkIdentifier?cap_first}DataControllerApi;

  constructor(configuration: Configuration | undefined) {
    this.openApiDataController = new ${frameworkIdentifier?cap_first}DataControllerApi(configuration);
  }

  getAllCompanyData(
    companyId: string,
    showOnlyActive?: boolean,
    reportingPeriod?: string,
    options?: AxiosRequestConfig,
  ): AxiosPromise<DataAndMetaInformation<${frameworkIdentifier?cap_first}Data>[]> {
    return this.openApiDataController.getAllCompany${frameworkIdentifier?cap_first}Data(companyId, showOnlyActive, reportingPeriod, options);
  }

  getFrameworkData(dataId: string, options?: AxiosRequestConfig): AxiosPromise<CompanyAssociatedData<${frameworkIdentifier?cap_first}Data>> {
    return this.openApiDataController.getCompanyAssociated${frameworkIdentifier?cap_first}Data(dataId, options);
  }

  postFrameworkData(
    data: CompanyAssociatedData<${frameworkIdentifier?cap_first}Data>,
    bypassQa?: boolean,
    options?: AxiosRequestConfig,
  ): AxiosPromise<DataMetaInformation> {
    return this.openApiDataController.postCompanyAssociated${frameworkIdentifier?cap_first}Data(data, bypassQa, options);
  }
}
