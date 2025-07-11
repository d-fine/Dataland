import { type AxiosRequestConfig, type AxiosPromise } from 'axios';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
import { type DataMetaInformation, type ExportFileType } from '@clients/backend';
import { type CompanyAssociatedData } from '@/api-models/CompanyAssociatedData';

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
