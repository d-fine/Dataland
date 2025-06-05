import { type AxiosRequestConfig, type AxiosPromise } from 'axios';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
import { type DataMetaInformation, type ExportFileType } from '@clients/backend';
import { type CompanyAssociatedData } from '@/api-models/CompanyAssociatedData';
import { type FrameworkDataTypes } from '@/utils/api/FrameworkDataTypes';

export interface BaseFrameworkDataApi<FrameworkDataType> {
  getAllCompanyData(
    companyId: string,
    showOnlyActive?: boolean,
    reportingPeriod?: string,
    options?: AxiosRequestConfig
  ): AxiosPromise<Array<DataAndMetaInformation<FrameworkDataType>>>;

  getFrameworkData(
    dataId: string,
    options?: AxiosRequestConfig
  ): AxiosPromise<CompanyAssociatedData<FrameworkDataType>>;
}

export interface PublicFrameworkDataApi<FrameworkDataType> extends BaseFrameworkDataApi<FrameworkDataType> {
  postFrameworkData(
    data: CompanyAssociatedData<FrameworkDataType>,
    bypassQa?: boolean,
    options?: AxiosRequestConfig
  ): AxiosPromise<DataMetaInformation>;

  exportCompanyAssociatedDataByDimensions(
    reportingPeriods: string[],
    companyIds: string[],
    fileFormat: ExportFileType,
    includeDataMetaInformation?: boolean,
    includeAlias?: boolean,
    options?: AxiosRequestConfig
    //eslint-disable-next-line @typescript-eslint/no-explicit-any
  ): AxiosPromise<any>;

  getCompanyAssociatedDataByDimensions(
    reportingPeriod: string,
    companyId: string,
    options?: AxiosRequestConfig
  ): AxiosPromise<CompanyAssociatedData<FrameworkDataType>>;
}

export interface PrivateFrameworkDataApi<FrameworkDataType> extends BaseFrameworkDataApi<FrameworkDataType> {
  getPrivateDocument(dataId: string, hash: string, options?: AxiosRequestConfig): AxiosPromise<File>;

  postFrameworkData(
    companyAssociatedSmeData: CompanyAssociatedData<FrameworkDataType>,
    documents: Array<File>,
    options?: AxiosRequestConfig
  ): AxiosPromise<DataMetaInformation>;
}

type OpenApiDataControllerApi<FrameworkNameObject, FrameworkDataType> = {
  [K in `getAllCompany${string & keyof FrameworkNameObject}`]: (
    companyId: string,
    showOnlyActive?: boolean,
    reportingPeriod?: string,
    options?: AxiosRequestConfig
  ) => AxiosPromise<Array<DataAndMetaInformation<FrameworkDataType>>>;
} & {
  [K in `getCompanyAssociated${string & keyof FrameworkNameObject}`]: (
    dataId: string,
    options?: AxiosRequestConfig
  ) => AxiosPromise<CompanyAssociatedData<FrameworkDataType>>;
} & {
  [K in `postCompanyAssociated${string & keyof FrameworkNameObject}`]: (
    data: CompanyAssociatedData<FrameworkDataType>,
    bypassQa?: boolean,
    options?: AxiosRequestConfig
  ) => AxiosPromise<DataMetaInformation>;
} & {
  [K in `exportCompanyAssociated${string & keyof FrameworkNameObject}ByDimensions`]: (
    reportingPeriods: string[],
    companyIds: string[],
    fileFormat: ExportFileType,
    includeDataMetaInformation?: boolean,
    options?: AxiosRequestConfig //eslint-disable-next-line @typescript-eslint/no-explicit-any
  ) => AxiosPromise<any>;
} & {
  [K in `getCompanyAssociated${string & keyof FrameworkNameObject}ByDimensions`]: (
    reportingPeriod: string,
    companyId: string,
    options?: AxiosRequestConfig
  ) => AxiosPromise<CompanyAssociatedData<FrameworkDataType>>;
};

class OpenApiUnificationAdapter<K extends keyof FrameworkDataTypes>
  implements PublicFrameworkDataApi<FrameworkDataTypes[K]['data']>
{
  private readonly apiSuffix: FrameworkDataTypes[K]['apiSuffix'];
  private readonly openApiDataController: OpenApiDataControllerApi<
    FrameworkNameObjectTranslation<FrameworkDataTypes[K]['apiSuffix']>,
    FrameworkDataTypes[K]['data']
  >;

  constructor(
    apiSuffix: FrameworkDataTypes[K]['apiSuffix'],
    openApiDataController: OpenApiDataControllerApi<
      FrameworkNameObjectTranslation<FrameworkDataTypes[K]['apiSuffix']>,
      FrameworkDataTypes[K]['data']
    >
  ) {
    this.apiSuffix = apiSuffix;
    this.openApiDataController = openApiDataController;
  }

  getAllCompanyData(
    companyId: string,
    showOnlyActive?: boolean,
    reportingPeriod?: string,
    options?: AxiosRequestConfig
  ): AxiosPromise<Array<DataAndMetaInformation<FrameworkDataTypes[K]['data']>>> {
    return this.openApiDataController[`getAllCompany${this.apiSuffix}`](
      companyId,
      showOnlyActive,
      reportingPeriod,
      options
    );
  }

  getFrameworkData(
    dataId: string,
    options?: AxiosRequestConfig
  ): AxiosPromise<CompanyAssociatedData<FrameworkDataTypes[K]['data']>> {
    return this.openApiDataController[`getCompanyAssociated${this.apiSuffix}`](dataId, options);
  }

  postFrameworkData(
    data: CompanyAssociatedData<FrameworkDataTypes[K]['data']>,
    bypassQa?: boolean,
    options?: AxiosRequestConfig
  ): AxiosPromise<DataMetaInformation> {
    return this.openApiDataController[`postCompanyAssociated${this.apiSuffix}`](data, bypassQa, options);
  }

  exportCompanyAssociatedDataByDimensions(
    reportingPeriods: string[],
    companyIds: string[],
    fileFormat: ExportFileType,
    includeMetaData?: boolean,
    includeAlias?: boolean,
    options?: AxiosRequestConfig
  ): // eslint-disable-next-line @typescript-eslint/no-explicit-any
  AxiosPromise<any> {
    return this.openApiDataController[`exportCompanyAssociated${this.apiSuffix}ByDimensions`](
      reportingPeriods,
      companyIds,
      fileFormat,
      includeMetaData,
      options
    );
  }

  getCompanyAssociatedDataByDimensions(
    reportingPeriod: string,
    companyId: string,
    options?: AxiosRequestConfig
  ): AxiosPromise<CompanyAssociatedData<FrameworkDataTypes[K]['data']>> {
    return this.openApiDataController[`getCompanyAssociated${this.apiSuffix}ByDimensions`](
      reportingPeriod,
      companyId,
      options
    );
  }
}

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
  apiSuffix: FrameworkDataTypes[K]['apiSuffix'],
  openApiDataController: OpenApiDataControllerApi<
    FrameworkNameObjectTranslation<FrameworkDataTypes[K]['apiSuffix']>,
    FrameworkDataTypes[K]['data']
  >
): PublicFrameworkDataApi<FrameworkDataTypes[K]['data']> {
  return new OpenApiUnificationAdapter(apiSuffix, openApiDataController);
}
