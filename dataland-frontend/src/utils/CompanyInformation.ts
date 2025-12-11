import type { ApiClientProvider } from '@/services/ApiClients.ts';
import { inject, ref, type Ref } from 'vue';
import type { CompanyIdAndName, CompanyInformation } from '@clients/backend';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { getErrorMessage } from '@/utils/ErrorMessageUtils';
import type Keycloak from 'keycloak-js';
import { getCompanyDataForFrameworkDataSearchPageWithoutFilters } from '@/utils/SearchCompaniesForFrameworkDataPageDataRequester.ts';

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
  parentCompany: Ref<CompanyIdAndName | null>; // ← Diese Zeile hinzufügen!
  waitingForData: Ref<boolean>;
  companyIdDoesNotExist: Ref<boolean>;
}> {
  const companyInformation = ref<CompanyInformation | null>(null);
  const hasParentCompany = ref(true);
  const companyIdDoesNotExist = ref(false);
  const waitingForData = ref(true);
  const parentCompany = ref<CompanyIdAndName | null>(null);
  const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

  if (!companyId)
    return {
      companyInformation,
      hasParentCompany,
      parentCompany,
      waitingForData,
      companyIdDoesNotExist,
    };

  try {
    const companyDataControllerApi = apiClientProvider.backendClients.companyDataController;
    companyInformation.value = (await companyDataControllerApi.getCompanyInfo(companyId)).data;
    if (companyInformation.value.parentCompanyLei == null) {
      hasParentCompany.value = false;
    } else {
      await getParentCompany(
        companyInformation.value.parentCompanyLei,
        hasParentCompany,
        parentCompany,
        assertDefined(getKeycloakPromise)
      );
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
  return {
    companyInformation,
    hasParentCompany,
    parentCompany,
    waitingForData,
    companyIdDoesNotExist,
  };
}

/**
 * Gets the parent company based on the lei
 * @param parentCompanyLei lei of the parent company
 * @param hasParentCompany boolean to indicate if the parent company exists
 * @param parentCompany company information of the parent company
 * @param getKeycloakPromise function to get the keycloak promise
 */
async function getParentCompany(
  parentCompanyLei: string,
  hasParentCompany: Ref<boolean>,
  parentCompany: Ref<CompanyIdAndName | null>,
  getKeycloakPromise: () => Promise<Keycloak>
): Promise<void> {
  console.log('getKeycloakPromise =', getKeycloakPromise);

  try {
    console.log(`Getting parent company with LEI: ${parentCompanyLei}`);

    const companyIdAndNames = await getCompanyDataForFrameworkDataSearchPageWithoutFilters(
      parentCompanyLei,
      getKeycloakPromise(),
      1
    );

    if (companyIdAndNames.length > 0) {
      parentCompany.value = companyIdAndNames[0]!;
      hasParentCompany.value = true;
    } else {
      parentCompany.value = null;
      hasParentCompany.value = false;
    }

    console.log(companyIdAndNames, ': parent company:');
  } catch {
    console.error(`Unable to find company with LEI: ${parentCompanyLei}`);
    parentCompany.value = null;
    hasParentCompany.value = false;
  }
}
