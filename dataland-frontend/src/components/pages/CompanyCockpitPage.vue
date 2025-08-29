<template>
  <TheContent class="flex">
    <CompanyInfoSheet :company-id="companyId" :show-single-data-request-button="true" />
    <Tabs v-model:value="activeTab">
      <TabList
        v-if="isCompanyMemberOrAdmin"
        :pt="{
          tabList: {
            style: 'display: flex; justify-content: center;',
          },
        }"
      >
        <Tab value="datasets" data-test="datasetsTab">Datasets</Tab>
        <Tab value="users" data-test="usersTab">Users</Tab>
      </TabList>
      <TabPanels>
        <TabPanel value="datasets">
          <div class="card-container">
            <div class="left-pane">
              <Card>
                <template #title>Latest Documents</template>
                <template #content>
                  <div class="card__separator" />
                  <div
                    v-for="(category, label) in DocumentMetaInfoDocumentCategoryEnum"
                    :key="category"
                    :data-test="category"
                  >
                    <div class="card__subtitle">{{ getPluralCategory(label.toString()) }}</div>
                    <div v-if="getDocumentData(category).length === 0">-</div>
                    <div v-else>
                      <div v-for="document in getDocumentData(category)" :key="document.documentId">
                        <DocumentDownloadLink
                          :document-download-info="{
                            downloadName: documentNameOrId(document),
                            fileReference: document.documentId,
                          }"
                          :label="documentNameOrId(document)"
                          :suffix="documentPublicationDateOrEmpty(document)"
                          show-icon
                        />
                      </div>
                    </div>
                  </div>
                </template>
                <template #footer>
                  <PrimeButton
                    label="VIEW ALL DOCUMENTS"
                    variant="text"
                    icon="pi pi-chevron-right"
                    icon-pos="right"
                    @click="routeToDocuments"
                  />
                </template>
              </Card>
            </div>

            <div class="right-pane">
              <div v-if="isClaimPanelVisible" class="claim-pane">
                <ClaimOwnershipPanel :company-id="companyId" />
              </div>
              <div class="frameworks-grid" data-test="summaryPanels">
                <FrameworkSummaryPanel
                  v-for="framework of frameworksToDisplay"
                  :key="framework"
                  :is-user-allowed-to-upload="isUserAllowedToUploadForFramework(framework)"
                  :company-id="companyId"
                  :framework="framework"
                  :number-of-provided-reporting-periods="
                    aggregatedFrameworkDataSummary?.[framework]?.numberOfProvidedReportingPeriods
                  "
                  :data-test="`${framework}-summary-panel`"
                />
              </div>

              <PrimeButton
                :label="showAllFrameworks ? 'SHOW LESS' : 'SHOW ALL'"
                data-test="toggleShowAll"
                @click="toggleShowAll"
                :icon="showAllFrameworks ? 'pi pi-angle-up' : 'pi pi-angle-down'"
                :pt="{
                  root: { style: 'margin-left: auto' },
                }"
                variant="link"
              />
            </div>
          </div>
        </TabPanel>
        <TabPanel v-if="isCompanyMemberOrAdmin" value="users">
          <CompanyRolesCard v-for="role in companyRoles" :key="role" :companyId="companyId" :role="role" />
        </TabPanel>
      </TabPanels>
    </Tabs>
  </TheContent>
  <TheFooter />
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, inject, unref } from 'vue';
import type { Ref } from 'vue';
import { useRouter } from 'vue-router';

import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import ClaimOwnershipPanel from '@/components/resources/companyCockpit/ClaimOwnershipPanel.vue';
import FrameworkSummaryPanel from '@/components/resources/companyCockpit/FrameworkSummaryPanel.vue';
import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';
import CompanyRolesCard from '@/components/resources/companyCockpit/CompanyRolesCard.vue';

import PrimeButton from 'primevue/button';
import Card from 'primevue/card';
import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';

import { ApiClientProvider } from '@/services/ApiClients';
import { hasCompanyAtLeastOneCompanyOwner } from '@/utils/CompanyRolesUtils';
import { ALL_FRAMEWORKS_IN_DISPLAYED_ORDER, MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants';
import { isFrameworkPublic } from '@/utils/Frameworks';
import { KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { documentNameOrId, documentPublicationDateOrEmpty, getPluralCategory } from '@/utils/StringFormatter';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { isCompanyIdValid } from '@/utils/ValidationUtils';
import type { AggregatedFrameworkDataSummary, DataTypeEnum } from '@clients/backend';
import { CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import {
  DocumentMetaInfoDocumentCategoryEnum,
  type DocumentMetaInfoResponse,
  SearchForDocumentMetaInformationDocumentCategoriesEnum,
} from '@clients/documentmanager';
import type Keycloak from 'keycloak-js';

const props = defineProps<{ companyId: string }>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const companyRoleAssignmentsRef = inject<Ref<CompanyRoleAssignmentExtended[] | undefined>>(
  'companyRoleAssignments',
  ref([])
);
const router = useRouter();

const activeTab = ref<'datasets' | 'users'>('datasets');
const isCompanyMemberOrAdmin = ref(false);
type SummaryByType = Partial<Record<DataTypeEnum, AggregatedFrameworkDataSummary>>;
const aggregatedFrameworkDataSummary = ref<SummaryByType>({});
const FRAMEWORKS_ALL = ALL_FRAMEWORKS_IN_DISPLAYED_ORDER;
const FRAMEWORKS_MAIN = MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER;
const showAllFrameworks = ref(false);
const isUserCompanyOwnerOrUploader = ref(false);
const isUserKeycloakUploader = ref(false);
const isAnyCompanyOwnerExisting = ref(false);
const isUserCompanyMember = ref(false);
const isUserDatalandAdmin = ref(false);

const latestDocuments = reactive<Record<string, DocumentMetaInfoResponse[]>>({});
Object.values(DocumentMetaInfoDocumentCategoryEnum).forEach((category) => {
  latestDocuments[`latest${category}`] = [];
});
const chunkSize = 3;
const companyRoles = [CompanyRole.MemberAdmin, CompanyRole.Member, CompanyRole.CompanyOwner, CompanyRole.DataUploader];
const isClaimPanelVisible = computed(() => !isAnyCompanyOwnerExisting.value && isCompanyIdValid(props.companyId));
const frameworksToDisplay = computed(() => (showAllFrameworks.value ? FRAMEWORKS_ALL : FRAMEWORKS_MAIN));

/**
 * Returns the document data array for a given category.
 */
const getDocumentData = (category: keyof typeof DocumentMetaInfoDocumentCategoryEnum): DocumentMetaInfoResponse[] => {
  return latestDocuments[`latest${category}`] || [];
};

/**
 * Fetches the aggregated framework data summary for the current company.
 * Updates the aggregatedFrameworkDataSummary ref with the response.
 */
async function getAggregatedFrameworkDataSummary(): Promise<void> {
  const api = new ApiClientProvider(assertDefined(getKeycloakPromise)()).backendClients.companyDataController;
  const response = await api.getAggregatedFrameworkDataSummary(props.companyId);
  aggregatedFrameworkDataSummary.value = response.data as SummaryByType;
}

/**
 * Fetches the latest document meta-information for each document category.
 * Populates the latestDocuments reactive object with the results.
 */
async function getMetaInfoForLatestDocuments(): Promise<void> {
  const api = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients.documentController;
  for (const value of Object.values(SearchForDocumentMetaInformationDocumentCategoriesEnum)) {
    const result = await api.searchForDocumentMetaInformation(props.companyId, new Set([value]), undefined, chunkSize);
    latestDocuments[`latest${value}`] = result.data;
  }
}

/**
 * Navigates to the document overview page for the current company.
 */
function routeToDocuments(): void {
  void router.push({ path: `/companies/${props.companyId}/documents` });
}

/**
 * Determines if the user is allowed to upload for a given framework.
 */
function isUserAllowedToUploadForFramework(framework: DataTypeEnum): boolean {
  return isUserCompanyOwnerOrUploader.value || (isFrameworkPublic(framework) && isUserKeycloakUploader.value);
}

/**
 * Sets user rights and roles for the current company.
 * Updates isAnyCompanyOwnerExisting, isUserCompanyOwnerOrUploader, and isUserKeycloakUploader.
 */
async function setUserRights(): Promise<void> {
  isAnyCompanyOwnerExisting.value = await hasCompanyAtLeastOneCompanyOwner(props.companyId, getKeycloakPromise);

  const assignments = unref(companyRoleAssignmentsRef) ?? [];
  const roles = assignments.filter((r) => r.companyId === props.companyId).map((r) => r.companyRole);

  isUserCompanyOwnerOrUploader.value =
    roles.includes(CompanyRole.CompanyOwner) || roles.includes(CompanyRole.DataUploader);
  isUserKeycloakUploader.value = await checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, getKeycloakPromise);
  isUserCompanyMember.value = roles.length > 0;
  isUserDatalandAdmin.value = await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise);
  isCompanyMemberOrAdmin.value = isUserCompanyMember.value || isUserDatalandAdmin.value;
}

/**
 * Toggles the display of all frameworks.
 */
function toggleShowAll(): void {
  showAllFrameworks.value = !showAllFrameworks.value;
}

watch(
  () => props.companyId,
  async (newId, oldId) => {
    if (newId == oldId) return;
    await getAggregatedFrameworkDataSummary();
    await getMetaInfoForLatestDocuments();
    await setUserRights();
  }
);

watch(activeTab, (val) => {
  const base = `/companies/${props.companyId}`;
  void router.replace({ path: val === 'users' ? `${base}/users` : base });
});

onMounted(async () => {
  await setUserRights();
  await getAggregatedFrameworkDataSummary();
  await getMetaInfoForLatestDocuments();

  const path = router.currentRoute.value.path;
  if (path.endsWith('/users') && !isCompanyMemberOrAdmin.value) {
    activeTab.value = 'datasets';
    await router.replace({ path: `/companies/${props.companyId}` });
  } else {
    activeTab.value = path.endsWith('/users') ? 'users' : 'datasets';
  }
});
</script>

<style lang="scss" scoped>
.card-container {
  display: flex;
  gap: var(--spacing-xxxl);
  padding: var(--spacing-xl) var(--spacing-xxl);
  background-color: var(--p-surface-50);
  align-items: flex-start;

  > .left-pane {
    flex: 0 0 30%;
  }

  > .right-pane {
    flex: 1 1 0;
    display: flex;
    flex-direction: column;
    gap: var(--spacing-md);
  }

  @media (max-width: 768px) {
    flex-direction: column;

    > .left-pane,
    > .right-pane {
      flex: none;
      width: 100%;
    }
  }
}

.claim-pane {
  margin-bottom: var(--spacing-md);
}

.frameworks-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-xxl);

  /* collapse to one column on smaller screens */
  @media (max-width: 1024px) {
    grid-template-columns: repeat(1, 1fr);
  }
}

.card {
  &__separator {
    width: 100%;
    border-bottom: #e0dfde solid 1px;
    margin-top: 8px;
    margin-bottom: 24px;
  }

  &__subtitle {
    font-size: 16px;
    font-weight: 700;
    line-height: 21px;
    margin-top: 8px;
  }
}

.d-letters {
  letter-spacing: 0.05em;
}

.text-primary {
  color: var(--main-color);
}
</style>
