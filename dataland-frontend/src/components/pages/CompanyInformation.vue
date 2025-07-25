<template>
  <div>
    <div v-if="waitingForData" class="inline-loading text-center">
      <p class="font-medium text-xl">Loading company information...</p>
      <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
    </div>
    <div v-else-if="companyInformation && !waitingForData" class="company-details">
      <div class="company-details__headline">
        <div class="left-elements">
          <h1 data-test="companyNameTitle">{{ companyInformation.companyName }}</h1>
          <div
            class="p-badge badge-light-green outline rounded"
            data-test="verifiedCompanyOwnerBadge"
            v-if="hasCompanyOwner"
          >
            <span class="material-icons-outlined fs-sm">verified</span>
            Verified Company Owner
          </div>
        </div>
        <div class="right-elements">
          <PrimeButton
            v-if="authenticated"
            class="primary-button"
            aria-label="Add company to your portfolios"
            @click="openPortfolioModal"
            data-test="addCompanyToPortfoliosButton"
          >
            <i class="pi pi-plus pr-2" />Add to a portfolio
          </PrimeButton>
          <SingleDataRequestButton :company-id="companyId" v-if="showSingleDataRequestButton" />
        </div>
      </div>

      <ClaimOwnershipDialog
        :company-id="companyId"
        :company-name="companyInformation.companyName"
        :dialog-is-open="dialogIsOpen"
        :claim-is-submitted="claimIsSubmitted"
        @claim-submitted="claimIsSubmitted = true"
        @close-dialog="onCloseDialog"
      />

      <div class="company-details__separator" />

      <div class="company-details__info-holder">
        <div class="company-details__info">
          <span>Sector: </span>
          <span class="font-semibold" data-test="sector-visible">{{ displaySector }}</span>
        </div>
        <div class="company-details__info">
          <span>Headquarter: </span>
          <span class="font-semibold" data-test="headquarter-visible">{{ companyInformation.headquarters }}</span>
        </div>
        <div class="company-details__info">
          <span>LEI: </span>
          <span class="font-semibold" data-test="lei-visible">{{ displayLei }}</span>
        </div>
        <div class="company-details__info">
          <span>Parent Company: </span>
          <span v-if="hasParentCompany" class="font-semibold" style="cursor: pointer">
            <a class="link" style="display: inline-flex" data-test="parent-visible" @click="visitParentCompany()">
              {{ parentCompany?.companyName }}</a
            ></span
          >
          <span v-if="!hasParentCompany" data-test="parent-visible" class="font-semibold">—</span>
        </div>
      </div>
    </div>
    <div v-else-if="companyIdDoesNotExist" class="col-12">
      <h1 class="mb-0" data-test="noCompanyWithThisIdErrorIndicator">No company with this ID present</h1>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ApiClientProvider } from '@/services/ApiClients';
import { computed, inject, onMounted, ref, watch } from 'vue';
import { type CompanyIdAndName, type CompanyInformation, IdentifierType } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import ClaimOwnershipDialog from '@/components/resources/companyCockpit/ClaimOwnershipDialog.vue';
import { getErrorMessage } from '@/utils/ErrorMessageUtils';
import SingleDataRequestButton from '@/components/resources/companyCockpit/SingleDataRequestButton.vue';
import { hasCompanyAtLeastOneCompanyOwner, hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import { getCompanyDataForFrameworkDataSearchPageWithoutFilters } from '@/utils/SearchCompaniesForFrameworkDataPageDataRequester';
import { CompanyRole } from '@clients/communitymanager';
import PrimeButton from 'primevue/button';
import router from '@/router';
import AddCompanyToPortfolios from '@/components/general/AddCompanyToPortfolios.vue';
import type { BasePortfolio } from '@clients/userservice';
import { useDialog } from 'primevue/usedialog';
import { type NavigationFailure } from 'vue-router';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const authenticated = inject<boolean>('authenticated');
const dialog = useDialog();

const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const emits = defineEmits(['fetchedCompanyInformation']);

const companyInformation = ref<CompanyInformation | null>(null);
const waitingForData = ref<boolean>(true);
const companyIdDoesNotExist = ref<boolean>(false);
const isUserCompanyOwner = ref<boolean>(false);
const hasCompanyOwner = ref<boolean>(false);
const dialogIsOpen = ref<boolean>(false);
const claimIsSubmitted = ref<boolean>(false);
const hasParentCompany = ref<boolean | undefined>(undefined);
const parentCompany = ref<CompanyIdAndName | null>(null);

let allUserPortfolios: BasePortfolio[] = [];

const displaySector = computed(() => {
  if (companyInformation.value?.sector) {
    return companyInformation.value?.sector;
  } else {
    return '—';
  }
});

const displayLei = computed(() => {
  return companyInformation.value?.identifiers?.[IdentifierType.Lei]?.[0] ?? '—';
});

const props = defineProps({
  companyId: {
    type: String,
    required: true,
  },
  showSingleDataRequestButton: {
    type: Boolean,
    default: false,
  },
});

onMounted(() => {
  fetchDataForThisPage();
});

watch(
  () => props.companyId,
  () => {
    fetchDataForThisPage();
  }
);

/**
 * A complete fetch of all data that is relevant for UI elements of this page
 */
function fetchDataForThisPage(): void {
  try {
    void getCompanyInformation();
    void setCompanyOwnershipStatus();
    void updateHasCompanyOwner();
    claimIsSubmitted.value = false;
  } catch (error) {
    console.error('Error fetching data for new company:', error);
  }
}

/**
 * Get the list of all portfolios of the current user.
 */
async function fetchUserPortfolios(): Promise<void> {
  try {
    allUserPortfolios = (await apiClientProvider.apiClients.portfolioController.getAllPortfoliosForCurrentUser()).data;
  } catch (error) {
    console.error(error);
    throw error;
  }
}

/**
 * Opens the modal for adding the company to which companyInformation refers to one or multiple
 * portfolios of the logged-in user.
 */
function openPortfolioModal(): void {
  fetchUserPortfolios()
    .then(() => {
      dialog.open(AddCompanyToPortfolios, {
        props: {
          header: 'Add company to a portfolio',
          modal: true,
        },
        data: {
          companyId: props.companyId,
          allUserPortfolios: allUserPortfolios,
        },
      });
    })
    .catch((error) => {
      console.error(error);
      throw error;
    });
}

/**
 * triggers route push to parent company if the parent company exists
 * @returns route push
 */
async function visitParentCompany(): Promise<void | NavigationFailure> {
  if (parentCompany.value) {
    const parentCompanyUrl = `/companies/${parentCompany.value.companyId}`;
    return router.push(parentCompanyUrl);
  }
}

/**
 * Updates the hasCompanyOwner in an async way
 */
async function updateHasCompanyOwner(): Promise<void> {
  hasCompanyOwner.value = await hasCompanyAtLeastOneCompanyOwner(props.companyId, getKeycloakPromise);
}

/**
 * Handles the close button click event of the dialog
 */
function onCloseDialog(): void {
  dialogIsOpen.value = false;
}

/**
 * Gets the parent company based on the lei
 * @param parentCompanyLei lei of the parent company
 */
async function getParentCompany(parentCompanyLei: string): Promise<void> {
  try {
    const companyIdAndNames = await getCompanyDataForFrameworkDataSearchPageWithoutFilters(
      parentCompanyLei,
      assertDefined(getKeycloakPromise)(),
      1
    );
    if (companyIdAndNames.length > 0) {
      parentCompany.value = companyIdAndNames[0];
      hasParentCompany.value = true;
    } else {
      hasParentCompany.value = false;
    }
  } catch {
    console.error(`Unable to find company with LEI: ${companyInformation.value?.parentCompanyLei}`);
  }
}

/**
 * Uses the dataland API to retrieve information about the company identified by the local
 * companyId object.
 */
async function getCompanyInformation(): Promise<void> {
  waitingForData.value = true;
  if (props.companyId === undefined) return;
  try {
    const companyDataControllerApi = apiClientProvider.backendClients.companyDataController;
    companyInformation.value = (await companyDataControllerApi.getCompanyInfo(props.companyId)).data;
    if (companyInformation.value.parentCompanyLei == null) {
      hasParentCompany.value = false;
    } else {
      await getParentCompany(companyInformation.value.parentCompanyLei);
    }
    emits('fetchedCompanyInformation', companyInformation.value);
  } catch (error) {
    console.error(error);
    if (getErrorMessage(error).includes('404')) {
      companyIdDoesNotExist.value = true;
    }
    companyInformation.value = null;
  } finally {
    waitingForData.value = false;
  }
}

/**
 * Set the company-ownership status of current user
 * @returns a void promise so that the setter-function can be awaited
 */
async function setCompanyOwnershipStatus(): Promise<void> {
  return hasUserCompanyRoleForCompany(CompanyRole.CompanyOwner, props.companyId, getKeycloakPromise).then((result) => {
    isUserCompanyOwner.value = result;
  });
}
</script>

<style scoped lang="scss">
@use '@/assets/scss/newVariables';
@use '@/assets/scss/variables';

.inline-loading {
  width: 450px;
}

.rounded {
  border-radius: 0.5rem;
}

.company-details {
  display: flex;
  flex-direction: column;
  width: 100%;

  &__headline {
    display: flex;
    justify-content: space-between;
    flex-direction: row;
    align-items: center;
  }

  &__separator {
    @media only screen and (max-width: newVariables.$small) {
      width: 100%;
      border-bottom: #e0dfde 1px solid;
      margin-bottom: 0.5rem;
    }
  }

  &__info-holder {
    display: flex;
    flex-direction: row;
    @media only screen and (max-width: newVariables.$small) {
      flex-direction: column;
    }
  }

  &__info {
    padding-top: 0.3rem;
    @media only screen and (min-width: newVariables.$small) {
      padding-right: 40px;
    }
  }
}

.left-elements,
.right-elements {
  display: flex;
  align-items: center;
}

.fs-sm {
  font-size: variables.$fs-sm;
  margin-right: 0.25rem;
}
</style>
