import { ApiClientProvider } from '@/services/ApiClients.ts';
import { inject, ref, type Ref, unref } from 'vue';
import { type CompanyIdAndName, CompanyInformation } from '@clients/backend';
import { getCompanyDataForFrameworkDataSearchPageWithoutFilters } from '@/utils/SearchCompaniesForFrameworkDataPageDataRequester.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { getErrorMessage } from '@/utils/ErrorMessageUtils';
import type Keycloak from 'keycloak-js';

/**
 * Gets the company information based on the company id
 * @param companyId Company ID of the company whose information is to be fetched
 * @param apiClientProvider api client provider to use for the connection
 */
export async function getCompanyInformation(
  companyId: string,
  apiClientProvider: ApiClientProvider
): Promise<{
  companyInformation: Ref<CompanyInformation | null>;
  hasParentCompany: Ref<boolean>;
  waitingForData: Ref<boolean>;
  companyIdDoesNotExist: Ref<boolean>;
}> {
  const companyInformation = ref<CompanyInformation | null>(null);
  const hasParentCompany = ref(true);
  const companyIdDoesNotExist = ref(false);
  const waitingForData = ref(true);

  if (!companyId) return { companyInformation, hasParentCompany, waitingForData, companyIdDoesNotExist };

  try {
    const companyDataControllerApi = apiClientProvider.backendClients.companyDataController;
    companyInformation.value = (await companyDataControllerApi.getCompanyInfo(companyId)).data;
    if (companyInformation.value.parentCompanyLei == null) {
      hasParentCompany.value = false;
    } else {
      await getParentCompany(companyInformation.value.parentCompanyLei, unref(hasParentCompany));
    }
  } catch (error) {
    console.error(error);
    if (getErrorMessage(error).includes('404')) {
      companyIdDoesNotExist.value = true;
    }
    companyInformation.value = null;
  } finally {
    waitingForData.value = false;
  }
  return { companyInformation, hasParentCompany, waitingForData, companyIdDoesNotExist };
}

/**
 * Gets the parent company based on the lei
 * @param parentCompanyLei lei of the parent company
 * @param hasParentCompany boolean to indicate if the parent company exists
 *
 */

async function getParentCompany(
  parentCompanyLei: string,
  hasParentCompany: boolean
): Promise<{ hasParentCompany: boolean; parentCompany: CompanyIdAndName | null }> {
  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
  let parentCompany: CompanyIdAndName | null = null;

  try {
    const companyIdAndNames = await getCompanyDataForFrameworkDataSearchPageWithoutFilters(
      parentCompanyLei,
      assertDefined(getKeycloakPromise)(),
      1
    );
    if (companyIdAndNames.length > 0) {
      parentCompany = companyIdAndNames[0]!;
      hasParentCompany = true;
    } else {
      hasParentCompany = false;
    }
  } catch {
    console.error(`Unable to find company with LEI: ${parentCompanyLei}`);
  }

  return { hasParentCompany, parentCompany };
}
