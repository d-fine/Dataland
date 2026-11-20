<template>
  <div class="card-container">
    <div class="left-pane">
      <Card>
        <template #title>Latest Documents</template>
        <template #content>
          <div class="card__separator" />
          <div v-for="(category, label) in DocumentMetaInfoDocumentCategoryEnum" :key="category" :data-test="category">
            <div class="card__subtitle">{{ getPluralCategory(label.toString()) }}</div>
            <div v-if="latestDocumentsByCategory[category].length === 0">-</div>
            <div v-else>
              <div v-for="document in latestDocumentsByCategory[category]" :key="document.documentId">
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
          :is-user-allowed-to-upload="isUserAllowedToUpload"
          :company-id="companyId"
          :framework="framework"
          :number-of-provided-reporting-periods="
            aggregatedFrameworkDataSummary?.[framework]?.numberOfProvidedReportingPeriods
          "
          :number-of-non-sourceable-reporting-periods="nonSourceablePeriodsByFramework[framework]?.length ?? 0"
          :non-sourceable-reporting-periods="nonSourceablePeriodsByFramework[framework]"
          :data-test="`${framework}-summary-panel`"
        />
      </div>
      <button
        type="button"
        class="document-button cursor-pointer flex flex-row align-items-center justify-content-end button-reset"
        aria-label="Toggle show all frameworks"
        @click="toggleShowAll"
        style="margin-left: auto"
      >
        <span class="text-primary font-semibold d-letters" :data-test="'toggleShowAll'">
          {{ showAllFrameworks ? 'SHOW LESS' : 'SHOW ALL' }}
        </span>
        <i class="material-icons text-primary">
          {{ showAllFrameworks ? 'expand_less' : 'expand_more' }}
        </i>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

import Card from 'primevue/card';
import PrimeButton from 'primevue/button';

import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';
import FrameworkSummaryPanel from '@/components/resources/companyCockpit/FrameworkSummaryPanel.vue';
import ClaimOwnershipPanel from '@/components/resources/companyCockpit/ClaimOwnershipPanel.vue';

import { useGetAggregatedFrameworkDataSummaryQuery } from '@/api-queries/backend/company-data/useGetAggregatedFrameworkDataSummaryQuery';
import { useSearchNonSourceableDimensionsQuery } from '@/api-queries/backend/non-sourceability/useSearchNonSourceableDimensionsQuery';
import { useGetExtendedCompanyRoleAssignmentsQuery } from '@/api-queries/community-manager/company-roles/useGetExtendedCompanyRoleAssignmentsQuery';
import { useHasCompanyAtLeastOneOwnerQuery } from '@/api-queries/community-manager/company-roles/useHasCompanyAtLeastOneOwnerQuery';
import { useSearchForDocumentMetaInformationQuery } from '@/api-queries/document-manager/document/useSearchForDocumentMetaInformationQuery';
import { isCompanyIdValid } from '@/utils/ValidationUtils';
import { getPluralCategory, documentNameOrId, documentPublicationDateOrEmpty } from '@/utils/StringFormatter';
import { ALL_FRAMEWORKS_IN_DISPLAYED_ORDER, MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { useCurrentUser } from '@/utils/useCurrentUser';

import type { DataTypeEnum } from '@clients/backend';
import { CompanyRole } from '@clients/communitymanager';
import {
  DocumentMetaInfoDocumentCategoryEnum,
  type DocumentMetaInfoResponse,
  SearchForDocumentMetaInformationDocumentCategoriesEnum,
} from '@clients/documentmanager';

const NUMBER_OF_LATEST_DOCUMENTS_PER_CATEGORY = 3;

const props = defineProps<{ companyId: string }>();

const router = useRouter();
const { userId: currentUserId, roles: currentUserRoles } = useCurrentUser();

const { data: aggregatedFrameworkDataSummary } = useGetAggregatedFrameworkDataSummaryQuery({
  companyId: () => props.companyId,
});

const nonSourceabilityRequest = computed(() => ({ companyIds: [props.companyId] }));
const { data: nonSourceableDimensions } = useSearchNonSourceableDimensionsQuery({
  dataDimensionSearchRequest: nonSourceabilityRequest,
});

const nonSourceablePeriodsByFramework = computed<Partial<Record<DataTypeEnum, string[]>>>(() => {
  const grouped: Partial<Record<DataTypeEnum, string[]>> = {};
  for (const dim of nonSourceableDimensions.value ?? []) {
    const dataType = dim.dataType as DataTypeEnum;
    grouped[dataType] ??= [];
    grouped[dataType]!.push(dim.reportingPeriod);
  }
  for (const key of Object.keys(grouped) as DataTypeEnum[]) {
    grouped[key]?.sort();
  }
  return grouped;
});

const { data: hasCompanyOwner } = useHasCompanyAtLeastOneOwnerQuery(
  { companyId: () => props.companyId },
  { enabled: computed(() => isCompanyIdValid(props.companyId)) }
);
const isClaimPanelVisible = computed(() => hasCompanyOwner.value === false);

const { data: currentUserRoleAssignments } = useGetExtendedCompanyRoleAssignmentsQuery({ userId: currentUserId });
const isUserAllowedToUpload = computed(() => {
  const isUserCompanyOwnerOrUploader = (currentUserRoleAssignments.value ?? []).some(
    (assignment) =>
      assignment.companyId === props.companyId &&
      (assignment.companyRole === CompanyRole.CompanyOwner || assignment.companyRole === CompanyRole.DataUploader)
  );
  return isUserCompanyOwnerOrUploader || currentUserRoles.value.includes(KEYCLOAK_ROLE_UPLOADER);
});

const latestDocumentQueries = Object.values(SearchForDocumentMetaInformationDocumentCategoriesEnum).map((category) => ({
  category,
  query: useSearchForDocumentMetaInformationQuery({
    companyId: () => props.companyId,
    documentCategories: [category],
    chunkSize: NUMBER_OF_LATEST_DOCUMENTS_PER_CATEGORY,
  }),
}));
const latestDocumentsByCategory = computed(() => {
  const documentsByCategory = {} as Record<DocumentMetaInfoDocumentCategoryEnum, DocumentMetaInfoResponse[]>;
  for (const { category, query } of latestDocumentQueries) {
    documentsByCategory[category] = query.data.value ?? [];
  }
  return documentsByCategory;
});

const showAllFrameworks = ref(false);
const frameworksToDisplay = computed(() =>
  showAllFrameworks.value ? ALL_FRAMEWORKS_IN_DISPLAYED_ORDER : MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER
);

/**
 * Toggles the display of all frameworks.
 */
function toggleShowAll(): void {
  showAllFrameworks.value = !showAllFrameworks.value;
}

/**
 * Navigates to the document overview page for the current company.
 */
function routeToDocuments(): void {
  void router.push({ path: `/companies/${props.companyId}/documents` });
}
</script>

<style scoped lang="scss">
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
