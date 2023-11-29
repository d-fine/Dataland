import { type AxiosPromise, type AxiosRequestConfig } from "axios";
import { type CompanyAssociatedData } from "@/api-models/CompanyAssociatedData";
import { type Configuration, type DataMetaInformation, type GdvData, GdvDataControllerApi } from "@clients/backend";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";

export class GdvApiClient implements FrameworkDataApi<GdvData> {
  private readonly openApiDataController: GdvDataControllerApi;

  constructor(configuration: Configuration | undefined) {
    this.openApiDataController = new GdvDataControllerApi(configuration);
  }

  getAllCompanyData(
    companyId: string,
    showOnlyActive?: boolean,
    reportingPeriod?: string,
    options?: AxiosRequestConfig,
  ): AxiosPromise<DataAndMetaInformation<GdvData>[]> {
    return this.openApiDataController.getAllCompanyGdvData(companyId, showOnlyActive, reportingPeriod, options);
  }

  getFrameworkData(dataId: string, options?: AxiosRequestConfig): AxiosPromise<CompanyAssociatedData<GdvData>> {
    return this.openApiDataController.getCompanyAssociatedGdvData(dataId, options);
  }

  postFrameworkData(
    data: CompanyAssociatedData<GdvData>,
    bypassQa?: boolean,
    options?: AxiosRequestConfig,
  ): AxiosPromise<DataMetaInformation> {
    return this.openApiDataController.postCompanyAssociatedGdvData(data, bypassQa, options);
  }
}
