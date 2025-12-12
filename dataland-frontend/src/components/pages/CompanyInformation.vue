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
              :pt="{ root: { style: 'padding-left: 0;' }, label: { style: 'font-weight: var(--font-weight-semibold);' } }"
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
import { computed, inject, ref, watch } from 'vue';
import { useQuery } from '@tanstack/vue-query'; // Import useQuery directly
import AddCompanyToPortfolios from '@/components/general/AddCompanyToPortfolios.vue';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import ClaimOwnershipDialog from '@/components/resources/companyCockpit/ClaimOwnershipDialog.vue';
import router from '@/router';
import { ApiClientProvider } from '@/services/ApiClients';
import { hasCompanyAtLeastOneCompanyOwner, hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type DataTypeEnum, IdentifierType } from '@clients/backend';
import { CompanyRole } from '@clients/communitymanager';
import type { BasePortfolio } from '@clients/userservice';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Tag from 'primevue/tag';
import { useDialog } from 'primevue/usedialog';
import { type NavigationFailure, type RouteLocationNormalizedLoaded } from 'vue-router';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils.ts';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles.ts';
import { useCompanyInformationQuery } from "@/queries/composables/useCompanyInformationQuery.ts";
import axios from 'axios';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const authenticated = inject<boolean>('authenticated');
const dialog = useDialog();
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const emits = defineEmits(['fetchedCompanyInformation']);

const props = defineProps({
  companyId: { type: String, required: true },
  showSingleDataRequestButton: { type: Boolean, default: false },
});

const companyIdRef = computed(() => props.companyId);


/**
 * Query: Company Information
 */
const {
  data: companyInformation,
  isPending: isCompanyInformationPending,
  isError: isCompanyInformationError,
  error: companyError,
} = useCompanyInformationQuery(companyIdRef, apiClientProvider);

/**
 * Queries: Company Ownership, Dataland Member, and User Company Role
 */

const hasCompanyOwnerQueryKey = computed(() => ['hasCompanyOwner', companyIdRef.value] as const);
const { data: hasCompanyOwner } = useQuery<boolean>({
  queryKey: hasCompanyOwnerQueryKey,
  enabled: computed(() => !!companyIdRef.value),
  queryFn: () => hasCompanyAtLeastOneCompanyOwner(companyIdRef.value, getKeycloakPromise),
  initialData: false,
});

const { data: isAdmin } = useQuery({
  queryKey: ['userIsAdmin'],
  queryFn: () => checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise),
  initialData: false,
  staleTime: Infinity
});

const userCompanyRolesKey = computed(() => ['userCompanyRoles', companyIdRef.value] as const);
const { data: userCompanyRoles } = useQuery({
  queryKey: userCompanyRolesKey,
  enabled: computed(() => !!companyIdRef.value && !!authenticated),
  queryFn: async () => {
    const keycloak = await getKeycloakPromise();
    const userId = keycloak.idTokenParsed?.sub;
    if (!userId) return [];

    const response = await apiClientProvider.apiClients.companyRolesController.getExtendedCompanyRoleAssignments(
        undefined,
        companyIdRef.value,
        userId
    );
    return response.data;
  },
  initialData: []
});

const companyRightsKey = computed(() => ['companyRights', companyIdRef.value] as const);
const { data: isDatalandMember } = useQuery<boolean>({
  queryKey: companyRightsKey,
  enabled: computed(() => !!companyIdRef.value),
  queryFn: async () => {
    const response = await apiClientProvider.apiClients.companyRightsController.getCompanyRights(companyIdRef.value);
    return response.data.some((right) => right.includes('Member'));
  },
  initialData: false
});

const isUserCompanyOwnerKey = computed(() => ['isUserCompanyOwner', companyIdRef.value] as const);
const { data: isUserCompanyOwner } = useQuery<boolean>({
  queryKey: isUserCompanyOwnerKey,
  enabled: computed(() => !!companyIdRef.value && !!authenticated),
  queryFn: () => hasUserCompanyRoleForCompany(CompanyRole.CompanyOwner, companyIdRef.value, getKeycloakPromise),
  initialData: false
});


const isMemberOfCompanyOrAdmin = computed(() => {
  return isAdmin.value || (userCompanyRoles.value && userCompanyRoles.value.length > 0);
});

const companyIdDoesNotExist = computed(() => {
  if (!isCompanyInformationError.value) return false;
  if (axios.isAxiosError(companyError.value)) {
    return companyError.value.response?.status === 404;
  }
  return false;
});

const displaySector = computed(() => companyInformation.value?.sector ?? '—');
const displayLei = computed(() => companyInformation.value?.identifiers?.[IdentifierType.Lei]?.[0] ?? '—');

const hasParentCompany = computed(() => !!companyInformation.value?.parentCompanyLei);
const parentCompany = computed(() => (companyInformation.value as any)?.parentCompany ?? null);

const dialogIsOpen = ref(false);
const claimIsSubmitted = ref(false);
let allUserPortfolios: BasePortfolio[] = [];


watch(companyInformation, (val) => {
  if (val) emits('fetchedCompanyInformation', val);
});


function handleSingleDataRequest(): Promise<NavigationFailure | void | undefined> {
  const dataType = router.currentRoute.value.params.dataType;
  return router.push({
    path: `/singledatarequest/${props.companyId}`,
    query: { preSelectedFramework: dataType ? (dataType as DataTypeEnum) : '' },
  });
}

function openPortfolioModal(): void {
  apiClientProvider.apiClients.portfolioController.getAllPortfoliosForCurrentUser()
      .then((res) => {
        allUserPortfolios = res.data;
        dialog.open(AddCompanyToPortfolios, {
          props: { header: 'Add company to a portfolio', modal: true },
          data: { companyId: props.companyId, allUserPortfolios },
        });
      })
      .catch(console.error);
}

async function visitParentCompany(): Promise<void | NavigationFailure> {
  if (parentCompany.value?.companyId) {
    return router.push(`/companies/${parentCompany.value.companyId}`);
  }
}

function onCloseDialog(): void {
  dialogIsOpen.value = false;
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