import {
  Configuration,
  type DataMetaInformation,
  type CompanyInformation,
  MetaDataControllerApi,
} from '@clients/backend';
import { type UploadIds } from '@e2e/utils/GeneralApiUtils';
import { getOrUploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { type BasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkDefinition';
import {
  type DataPointQaReport,
  DataPointQaReportControllerApi,
  type QaReportDataPointString,
} from '@clients/qaservice';

export type PublicApiClientConstructor<FrameworkDataType> = (
  config: Configuration
) => PublicFrameworkDataApi<FrameworkDataType>;

/**
 * Uploads a single framework entry for a company
 * @param frameworkDefinition The framework definition to upload data for
 * @param token The API bearer token to use
 * @param companyId The id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 * @param bypassQa Whether to bypass the QA process
 * @returns a promise on the created data meta information
 */
export async function uploadFrameworkDataForPublicToolboxFramework<FrameworkDataType>(
  frameworkDefinition: BasePublicFrameworkDefinition<FrameworkDataType>,
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: FrameworkDataType,
  bypassQa = true
): Promise<DataMetaInformation> {
  return uploadGenericFrameworkData(
    token,
    companyId,
    reportingPeriod,
    data,
    (config) => frameworkDefinition.getPublicFrameworkApiClient(config),
    bypassQa
  );
}

/**
 * Uploads a single framework entry for a company
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 * @param apiClientConstructor a function for retrieving the API client of the specified framework
 * @param bypassQa Whether to bypass the QA process
 * @returns a promise on the created data meta information
 */
export async function uploadGenericFrameworkData<FrameworkDataType>(
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: FrameworkDataType,
  apiClientConstructor: PublicApiClientConstructor<FrameworkDataType>,
  bypassQa: boolean = true
): Promise<DataMetaInformation> {
  const apiClient = apiClientConstructor(new Configuration({ accessToken: token }));

  const response = await apiClient.postFrameworkData(
    {
      companyId,
      reportingPeriod,
      data,
    },
    bypassQa
  );
  return response.data;
}

/**
 * Uploads a company and single framework data entry for a company
 * @param frameworkDefinition The framework to upload data for
 * @param token The API bearer token to use
 * @param companyInformation The company information to use for the company upload
 * @param testData The Dataset to upload
 * @param reportingPeriod The reporting period to use for the upload
 * @param bypassQa Whether to bypass the QA process
 * @returns an object which contains the companyId from the company upload and the dataId from the data upload
 */
export async function uploadCompanyAndFrameworkDataForPublicToolboxFramework<FrameworkDataType>(
  frameworkDefinition: BasePublicFrameworkDefinition<FrameworkDataType>,
  token: string,
  companyInformation: CompanyInformation,
  testData: FrameworkDataType,
  reportingPeriod: string,
  bypassQa = true
): Promise<UploadIds> {
  return getOrUploadCompanyViaApi(token, companyInformation).then(async (storedCompany) => {
    return uploadFrameworkDataForPublicToolboxFramework(
      frameworkDefinition,
      token,
      storedCompany.companyId,
      reportingPeriod,
      testData,
      bypassQa
    ).then((dataMetaInformation) => {
      return { companyId: storedCompany.companyId, dataId: dataMetaInformation.dataId };
    });
  });
}

/**
 * Uploads QA reports for data points contained in a dataset.
 *
 * The method retrieves all contained data points for the given `dataId`,
 * maps each entry in `qaReports` by key to the corresponding data point id,
 * and uploads the reports in chunks of 10.
 * Entries without a matching data point id are skipped.
 *
 * @param token Bearer token used for authenticated API requests.
 * @param dataId Identifier of the dataset whose data points should receive QA reports.
 * @param qaReports Map of data point keys to QA report payloads.
 * @returns A list of successfully uploaded QA report entities.
 */
export async function uploadQaReportsData(
  token: string,
  dataId: string,
  qaReports: { [key: string]: QaReportDataPointString }
): Promise<Array<DataPointQaReport>> {
  const metadataApi = new MetaDataControllerApi(new Configuration({ accessToken: token }));
  const response = await metadataApi.getContainedDataPoints(dataId);
  const dataPointIdMappings = response.data;
  const uploadedQaReports: Array<DataPointQaReport> = [];

  await Promise.all(
    Object.entries(qaReports).map(async ([key, value]) => {
      const dataPointId = dataPointIdMappings[key];
      if (dataPointId) {
        uploadedQaReports.push(await uploadSingleQaReportData(token, dataPointId, value));
      }
    })
  );

  return uploadedQaReports;
}

/**
 * Uploads a single QA report for a specific data point.
 *
 * Creates an authenticated QA service client using the provided bearer token,
 * submits the QA report payload for the given data point, and returns the
 * created QA report entity from the API response.
 *
 * @param token Bearer token used for authenticated API access.
 * @param dataPointId Identifier of the data point that receives the QA report.
 * @param qaReport QA report payload to upload for the data point.
 * @returns The persisted QA report returned by the backend.
 */
export async function uploadSingleQaReportData(
  token: string,
  dataPointId: string,
  qaReport: QaReportDataPointString
): Promise<DataPointQaReport> {
  const qaReportApi = new DataPointQaReportControllerApi(new Configuration({ accessToken: token }));
  const response = await qaReportApi.postQaReport(dataPointId, qaReport);
  return response.data;
}
