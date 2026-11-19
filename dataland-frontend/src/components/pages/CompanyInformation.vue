<template>
  <div>
    <div v-if="isCompanyInformationPending" class="inline-loading text-center">
      <p class="font-medium text-xl">Loading company information...</p>
      <DatalandProgressSpinner />
    </div>
    <div v-else-if="companyInformation" class="company-details">
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
          <Tag
            v-if="isDatalandMember && isMemberOfCompanyOrAdmin"
            data-test="datalandMemberBadge"
            value="Dataland Member"
            icon="pi pi-star"
            severity="warning"
          />
        </div>
        <div class="right-elements">
          <PrimeButton
            v-if="isAuthenticated && showAddToPortfolioButton"
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
            v-if="parentCompany"
            :label="parentCompany.companyName"
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
    <div v-else-if="isCompanyNotFound" class="col-12">
      <h1 class="mb-0" data-test="noCompanyWithThisIdErrorIndicator">No company with this ID present</h1>
    </div>
    <div v-else-if="isCompanyInformationError" class="col-12" data-test="companyInformationErrorIndicator">
      <p class="font-medium text-xl">The company information could not be loaded.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useGetCompaniesBySearchStringQuery } from '@/api-queries/backend/company-data/useGetCompaniesBySearchStringQuery';
import { useGetCompanyInfoQuery } from '@/api-queries/backend/company-data/useGetCompanyInfoQuery';
import { useGetCompanyRightsQuery } from '@/api-queries/community-manager/company-rights/useGetCompanyRightsQuery';
import { useGetExtendedCompanyRoleAssignmentsQuery } from '@/api-queries/community-manager/company-roles/useGetExtendedCompanyRoleAssignmentsQuery';
import { useHasCompanyAtLeastOneOwnerQuery } from '@/api-queries/community-manager/company-roles/useHasCompanyAtLeastOneOwnerQuery';
import AddCompanyToPortfolios from '@/components/general/AddCompanyToPortfolios.vue';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import router from '@/router';
import { getDisplayLei } from '@/utils/CompanyInformation.ts';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles.ts';
import { useCurrentUser } from '@/utils/useCurrentUser';
import { isCompanyIdValid } from '@/utils/ValidationUtils';
import { type CompanyInformation, type DataTypeEnum } from '@clients/backend';
import { isAxiosError } from 'axios';
import PrimeButton from 'primevue/button';
import Tag from 'primevue/tag';
import { useDialog } from 'primevue/usedialog';
import { computed, inject, toValue, watch, type MaybeRef } from 'vue';
import { type NavigationFailure, type RouteLocationNormalizedLoaded } from 'vue-router';

const MAX_RETRIES = 3;

const props = withDefaults(
  defineProps<{
    companyId: string;
    showSingleDataRequestButton?: boolean;
    showAddToPortfolioButton?: boolean;
  }>(),
  { showSingleDataRequestButton: false, showAddToPortfolioButton: true }
);

const emit = defineEmits<{ fetchedCompanyInformation: [companyInformation: CompanyInformation] }>();

const authenticated = inject<MaybeRef<boolean | undefined>>('authenticated');
const isAuthenticated = computed(() => toValue(authenticated) ?? false);
const dialog = useDialog();

const { userId: currentUserId, roles: currentUserRoles } = useCurrentUser();

const {
  data: companyInformation,
  isPending: isCompanyInformationPending,
  isError: isCompanyInformationError,
  error: companyInformationError,
} = useGetCompanyInfoQuery({ companyId: () => props.companyId }, { retry: retryUnlessNotFound });
const isCompanyNotFound = computed(() => isNotFoundError(companyInformationError.value));

const { data: parentCompanySearchResults } = useGetCompaniesBySearchStringQuery({
  searchString: () => companyInformation.value?.parentCompanyLei ?? undefined,
  resultLimit: 1,
});
const parentCompany = computed(() =>
  companyInformation.value?.parentCompanyLei ? parentCompanySearchResults.value?.[0] : undefined
);

const { data: hasCompanyOwner } = useHasCompanyAtLeastOneOwnerQuery(
  { companyId: () => props.companyId },
  { enabled: computed(() => isCompanyIdValid(props.companyId)) }
);

const { data: currentUserRoleAssignments } = useGetExtendedCompanyRoleAssignmentsQuery({ userId: currentUserId });
const isMemberOfCompanyOrAdmin = computed(
  () =>
    currentUserRoles.value.includes(KEYCLOAK_ROLE_ADMIN) ||
    (currentUserRoleAssignments.value ?? []).some((assignment) => assignment.companyId === props.companyId)
);

const { data: companyRights } = useGetCompanyRightsQuery(
  { companyId: () => props.companyId },
  { enabled: computed(() => !!props.companyId && isMemberOfCompanyOrAdmin.value) }
);
const isDatalandMember = computed(() => companyRights.value?.some((right) => right.includes('Member')) ?? false);

const displaySector = computed(() => companyInformation.value?.sector || '—');
const displayLei = computed(() => getDisplayLei(companyInformation.value ?? null));

watch(
  companyInformation,
  (loadedCompanyInformation) => {
    if (loadedCompanyInformation) emit('fetchedCompanyInformation', loadedCompanyInformation);
  },
  { immediate: true }
);

/**
 * Retries failed requests like the default retry behaviour, but not if the company does not exist.
 * @param failureCount the number of failed attempts so far
 * @param error the error of the last attempt
 * @returns true if the request shall be retried
 */
function retryUnlessNotFound(failureCount: number, error: Error): boolean {
  return !isNotFoundError(error) && failureCount < MAX_RETRIES;
}

/**
 * Checks whether a request failed because the requested resource does not exist.
 * @param error the error of a failed request
 * @returns true if the backend answered with 404
 */
function isNotFoundError(error: Error | null): boolean {
  return isAxiosError(error) && error.response?.status === 404;
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
 * Opens the modal for adding the company to one or multiple portfolios of the logged-in user.
 */
function openPortfolioModal(): void {
  dialog.open(AddCompanyToPortfolios, {
    props: {
      header: 'Add company to a portfolio',
      modal: true,
    },
    data: {
      companyId: props.companyId,
    },
  });
}

/**
 * triggers route push to parent company if the parent company exists
 * @returns route push
 */
async function visitParentCompany(): Promise<void | NavigationFailure> {
  if (parentCompany.value) {
    return router.push(`/companies/${parentCompany.value.companyId}`);
  }
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
