import { type AxiosPromise, type AxiosRequestConfig } from "axios";
import { type CompanyAssociatedData } from "@/api-models/CompanyAssociatedData";
import {
  type Configuration,
  type DataMetaInformation,
  type HeimathafenData,
  HeimathafenDataControllerApi,
} from "@clients/backend";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";

export class HeimathafenApiClient implements FrameworkDataApi<HeimathafenData> {
  private readonly openApiDataController: HeimathafenDataControllerApi;

  constructor(configuration: Configuration | undefined) {
    this.openApiDataController = new HeimathafenDataControllerApi(configuration);
  }

  getAllCompanyData(
    companyId: string,
    showOnlyActive?: boolean,
    reportingPeriod?: string,
    options?: AxiosRequestConfig,
  ): AxiosPromise<DataAndMetaInformation<HeimathafenData>[]> {
    return this.openApiDataController.getAllCompanyHeimathafenData(companyId, showOnlyActive, reportingPeriod, options);
  }

  getFrameworkData(dataId: string, options?: AxiosRequestConfig): AxiosPromise<CompanyAssociatedData<HeimathafenData>> {
    return this.openApiDataController.getCompanyAssociatedHeimathafenData(dataId, options);
  }

  postFrameworkData(
    data: CompanyAssociatedData<HeimathafenData>,
    bypassQa?: boolean,
    options?: AxiosRequestConfig,
  ): AxiosPromise<DataMetaInformation> {
    return this.openApiDataController.postCompanyAssociatedHeimathafenData(data, bypassQa, options);
  }
}
