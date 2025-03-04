import type { ExtendedStoredDataRequest, StoredDataRequest } from '@clients/communitymanager';
import { type ApiClientProvider } from '@/services/ApiClients.ts';
import { type DataMetaInformation, type DataTypeEnum } from '@clients/backend';
import { getParentCompanyId } from '@/utils/CompanyInformation.ts';

/**
 * Retrieve the meta data object of the active data set identified by the given parameters.
 *
 * This function may throw an exception.
 * @param companyId the company to which the dataset belongs
 * @param dataType the framework to search for
 * @param reportingPeriod the reporting period to search for
 * @param apiClientProvider an api client provider to use when polling the backend
 * @return the meta data object if found, else "undefined"
 */
async function getDataMetaInfo(
  companyId: string,
  dataType: string,
  reportingPeriod: string,
  apiClientProvider: ApiClientProvider
): Promise<DataMetaInformation | undefined> {
  const datasets = await apiClientProvider.backendClients.metaDataController.getListOfDataMetaInfo(
    companyId,
    dataType as DataTypeEnum,
    true,
    reportingPeriod
  );
  return datasets.data.length > 0 ? datasets.data[0] : undefined;
}

/**
 * Retrieves a URL to the data set that is answering the given request. This function may throw an exception.
 * @param storedDataRequest the data request whose ansering data set URL shall be found
 * @param apiClientProvider the ApiClientProvider to use for the connection
 */
export async function getAnsweringDataSetUrl(
  storedDataRequest: StoredDataRequest | ExtendedStoredDataRequest,
  apiClientProvider: ApiClientProvider
): Promise<string | undefined> {
  let answeringDataMetaInfo = await getDataMetaInfo(
    storedDataRequest.datalandCompanyId,
    storedDataRequest.dataType,
    storedDataRequest.reportingPeriod,
    apiClientProvider
  );
  if (answeringDataMetaInfo) {
    const parentCompanyId = await getParentCompanyId(storedDataRequest.datalandCompanyId, apiClientProvider);
    if (!parentCompanyId) return;
    answeringDataMetaInfo = await getDataMetaInfo(
      parentCompanyId,
      storedDataRequest.dataType,
      storedDataRequest.reportingPeriod,
      apiClientProvider
    );
  }
  if (answeringDataMetaInfo)
    return `/companies/${answeringDataMetaInfo.companyId}/frameworks/${answeringDataMetaInfo.dataType}`;
}
