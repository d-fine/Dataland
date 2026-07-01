import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { type ApiClientProvider } from '@/services/ApiClients';
import {
  getAllPublicFrameworkIdentifiers,
  getBasePublicFrameworkDefinition,
} from '@/frameworks/BasePublicFrameworkRegistry';

/**
 * Gets a framework data API for the framework specified by the identifier.
 * @param identifier the identifier of the framework
 * @param apiClientProvider should be an api client provider with an authenticated axios instance
 * @returns the framework data api for API calls to fetch framework data
 */
export function getFrameworkDataApiForIdentifier(
  identifier: string,
  apiClientProvider: ApiClientProvider
): PublicFrameworkDataApi<object> | undefined {
  const publicFrameworkIdentifiers = getAllPublicFrameworkIdentifiers();

  let dataControllerApi;
  if (publicFrameworkIdentifiers.includes(identifier)) {
    const frameworkDefinition = getBasePublicFrameworkDefinition(identifier);
    if (frameworkDefinition) {
      dataControllerApi = frameworkDefinition.getPublicFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
    }
  }
  return dataControllerApi;
}
