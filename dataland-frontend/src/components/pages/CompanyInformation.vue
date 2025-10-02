<template>
  <div>
    <div v-if="waitingForData" class="inline-loading text-center">
      <p class="font-medium text-xl">Loading company information...</p>
      <DatalandProgressSpinner />
    </div>
    <div v-else-if="companyInformation && !waitingForData" class="company-details">
      <div class="company-title-row">
        <div class="left-elements">
          <h1 data-test="companyNameTitle">{{ companyInformation.companyName }}</h1>
          <Tag
            v-if="hasCompanyOwner"
            data-test="verifiedCompanyOwnerBadge"
            value="Verified Company Owner"
            icon="pi pi-check-circle"
            severity="success"
          />
        </div>
        <div class="right-elements">
          <PrimeButton
            v-if="authenticated"
            icon="pi pi-plus"
            label="ADD TO PORTFOLIO"
            @click="openPortfolioModal"
            data-test="addCompanyToPortfoliosButton"
          />
          <PrimeButton
            v-if="showSingleDataRequestButton"
            icon="pi pi-file"
            label="REQUEST DATA"
            @click="handleSingleDataRequest"
            data-test="singleDataRequestButton"
          />
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

      <div class="company-info-row">
        <div>
          <span class="company-info-title">Sector:</span>
          <span class="company-info-content" data-test="sector-visible">{{ displaySector }}</span>
        </div>
        <div>
          <span class="company-info-title">Headquarter:</span>
          <span class="company-info-content" data-test="headquarter-visible">{{
            companyInformation.headquarters
          }}</span>
        </div>
        <div>
          <span class="company-info-title">LEI:</span>
          <span class="company-info-content" data-test="lei-visible">{{ displayLei }}</span>
        </div>
        <div>
          <span class="company-info-title">Parent Company: </span>
          <PrimeButton
            v-if="hasParentCompany"
            :label="parentCompany?.companyName"
            variant="link"
            data-test="parent-visible"
            @click="visitParentCompany"
            :pt="{
              root: {
                style: 'padding-left: 0;',
              },
              label: {
                style: 'font-weight: var(--font-weight-semibold);',
              },
            }"
          />
          <span v-else data-test="parent-visible" class="font-semibold">—</span>
        </div>
      </div>
    </div>
    <div v-else-if="companyIdDoesNotExist" class="col-12">
      <h1 class="mb-0" data-test="noCompanyWithThisIdErrorIndicator">No company with this ID present</h1>
    </div>
  </div>
</template>

<script setup lang="ts">
import AddCompanyToPortfolios from '@/components/general/AddCompanyToPortfolios.vue';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import ClaimOwnershipDialog from '@/components/resources/companyCockpit/ClaimOwnershipDialog.vue';
import router from '@/router';
import { ApiClientProvider } from '@/services/ApiClients';
import { hasCompanyAtLeastOneCompanyOwner, hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import { getErrorMessage } from '@/utils/ErrorMessageUtils';
import { getCompanyDataForFrameworkDataSearchPageWithoutFilters } from '@/utils/SearchCompaniesForFrameworkDataPageDataRequester';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type CompanyIdAndName, type CompanyInformation, type DataTypeEnum, IdentifierType } from '@clients/backend';
import { CompanyRole } from '@clients/communitymanager';
import type { BasePortfolio } from '@clients/userservice';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Tag from 'primevue/tag';
import { useDialog } from 'primevue/usedialog';
import { computed, inject, onMounted, ref, watch } from 'vue';
import { type NavigationFailure, type RouteLocationNormalizedLoaded } from 'vue-router';

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
 * Handles the click event of the single data request button.
 * Navigates to the single data request page with the companyId and preSelectedFramework as query parameters.
 * @returns a router push to the single data request page
 */
function handleSingleDataRequest(): Promise<NavigationFailure | void | undefined> {
  const currentRoute: RouteLocationNormalizedLoaded = router.currentRoute.value;
  const dataType = currentRoute.params.dataType;
  const preSelectedFramework = dataType ? (dataType as DataTypeEnum) : '';
  return router.push({
    path: `/singledatarequest/${props.companyId}`,
    query: {
      preSelectedFramework: preSelectedFramework,
    },
  });
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
      parentCompany.value = companyIdAndNames[0]!;
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

<style scoped>
.inline-loading {
  width: 28rem;
}

.company-title-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .left-elements,
  .right-elements {
    display: flex;
    flex-direction: row;
    justify-items: end;
    align-items: center;
    gap: var(--spacing-lg);
  }
}

.company-info-row {
  display: flex;
  flex-direction: row;
  justify-content: start;
  gap: var(--spacing-md);
  align-items: center;
  width: 100%;

  .company-info-title {
    padding-right: var(--spacing-xs);
  }

  .company-info-content {
    font-weight: var(--font-weight-semibold);
    padding-left: 0;
  }
}
</style>
