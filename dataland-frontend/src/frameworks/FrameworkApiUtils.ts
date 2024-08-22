import { type PrivateFrameworkDataApi, type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { type ApiClientProvider } from '@/services/ApiClients';
import {
  getAllPrivateFrameworkIdentifiers,
  getBasePrivateFrameworkDefinition,
} from '@/frameworks/BasePrivateFrameworkRegistry';
import {
  getAllPublicFrameworkIdentifiers,
  getBasePublicFrameworkDefinition,
} from '@/frameworks/BasePublicFrameworkRegistry';
import { type FrameworkDataTypes } from '@/utils/api/FrameworkDataTypes';

/**
 * Gets a framework data API for the framework specified by the identifier.
 * @param identifier the identifier of the framework
 * @param apiClientProvider should be an api client provider with an authenticated axios instance
 * @returns the framework data api for API calls to fetch framework data
 */
export function getFrameworkDataApiForIdentifier(
  identifier: string,
  apiClientProvider: ApiClientProvider
): PrivateFrameworkDataApi<object> | PublicFrameworkDataApi<object> | undefined {
  const privateFrameworkIdentifiers = getAllPrivateFrameworkIdentifiers();
  const publicFrameworkIdentifiers = getAllPublicFrameworkIdentifiers();

  let dataControllerApi;
  if (privateFrameworkIdentifiers.includes(identifier)) {
    const frameworkDefinition = getBasePrivateFrameworkDefinition(identifier);
    if (frameworkDefinition) {
      dataControllerApi = frameworkDefinition.getPrivateFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
    }
  } else if (publicFrameworkIdentifiers.includes(identifier)) {
    const frameworkDefinition = getBasePublicFrameworkDefinition(identifier);
    if (frameworkDefinition) {
      dataControllerApi = frameworkDefinition.getPublicFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
    }
  } else {
    // If code reaches this point, it means that the framework for the identifier is not integrated into the Toolbox yet
    dataControllerApi = apiClientProvider.getUnifiedFrameworkDataController(identifier as keyof FrameworkDataTypes);
  }
  return dataControllerApi;
}
