import { type AxiosRequestConfig, type AxiosPromise } from "axios";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";
import { type DataMetaInformation } from "@clients/backend";
import { type CompanyAssociatedData } from "@/api-models/CompanyAssociatedData";
import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";

export interface FrameworkDataApi<FrameworkDataType> {
  getAllCompanyData(
    companyId: string,
    showOnlyActive?: boolean,
    reportingPeriod?: string,
    options?: AxiosRequestConfig,
  ): AxiosPromise<Array<DataAndMetaInformation<FrameworkDataType>>>;
  getFrameworkData(
    dataId: string,
    options?: AxiosRequestConfig,
  ): AxiosPromise<CompanyAssociatedData<FrameworkDataType>>;
  postFrameworkData(
    data: CompanyAssociatedData<FrameworkDataType>,
    bypassQa?: boolean,
    options?: AxiosRequestConfig,
  ): AxiosPromise<DataMetaInformation>;
}

type OpenApiDataControllerApi<FrameworkNameObject, FrameworkDataType> = {
  [K in `getAllCompany${string & keyof FrameworkNameObject}`]: (
    companyId: string,
    showOnlyActive?: boolean,
    reportingPeriod?: string,
    options?: AxiosRequestConfig,
  ) => AxiosPromise<Array<DataAndMetaInformation<FrameworkDataType>>>;
} & {
  [K in `getCompanyAssociated${string & keyof FrameworkNameObject}`]: (
    dataId: string,
    options?: AxiosRequestConfig,
  ) => AxiosPromise<CompanyAssociatedData<FrameworkDataType>>;
} & {
  [K in `postCompanyAssociated${string & keyof FrameworkNameObject}`]: (
    data: CompanyAssociatedData<FrameworkDataType>,
    bypassQa?: boolean,
    options?: AxiosRequestConfig,
  ) => AxiosPromise<DataMetaInformation>;
};

type FrameworkNameObjectTranslation<K extends string> = {
  [key in K]: string;
};

/**
 * This function takes an openapi-generated framework data controller in combination with the
 * framework specific api suffix and constructs a unified framework data controller in a type-safe fashion.
 * @param apiSuffix The suffix of the individual framework (i.e., the openapi-controller has a function getAllCompany<SUFFIX>)
 * @param openApiDataController the openapi-generated controller
 * @returns the unified controller interface
 */
export function translateFrameworkApi<K extends keyof FrameworkDataTypes>(
  apiSuffix: FrameworkDataTypes[K]["apiSuffix"],
  openApiDataController: OpenApiDataControllerApi<
    FrameworkNameObjectTranslation<FrameworkDataTypes[K]["apiSuffix"]>,
    FrameworkDataTypes[K]["data"]
  >,
): FrameworkDataApi<FrameworkDataTypes[K]["data"]> {
  return {
    getAllCompanyData: openApiDataController[`getAllCompany${apiSuffix}`],
    getFrameworkData: openApiDataController[`getCompanyAssociated${apiSuffix}`],
    postFrameworkData: openApiDataController[`postCompanyAssociated${apiSuffix}`],
  };
}
