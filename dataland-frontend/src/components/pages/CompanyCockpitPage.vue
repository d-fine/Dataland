<template>
  <TheHeader v-if="!useMobileView" />
  <TheContent class="flex">
    <CompanyInfoSheet :company-id="companyId" :show-single-data-request-button="true" />
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
        @click="toggleShowAll"
        :icon="showAllFrameworks ? 'pi pi-angle-up' : 'pi pi-angle-down'"
        style="margin-left: auto"
        />
      </div>
    </div>
  </TheContent>
  <TheFooter />
</template>

<script lang="ts">
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import ClaimOwnershipPanel from '@/components/resources/companyCockpit/ClaimOwnershipPanel.vue';
import FrameworkSummaryPanel from '@/components/resources/companyCockpit/FrameworkSummaryPanel.vue';
import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { hasCompanyAtLeastOneCompanyOwner } from '@/utils/CompanyRolesUtils';
import { ALL_FRAMEWORKS_IN_DISPLAYED_ORDER, MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants';
import { isFrameworkPublic } from '@/utils/Frameworks';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { documentNameOrId, documentPublicationDateOrEmpty, getPluralCategory } from '@/utils/StringFormatter';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { isCompanyIdValid } from '@/utils/ValidationUtils';
import { type AggregatedFrameworkDataSummary, type DataTypeEnum } from '@clients/backend';
import { CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import {
  DocumentMetaInfoDocumentCategoryEnum,
  type DocumentMetaInfoResponse,
  SearchForDocumentMetaInformationDocumentCategoriesEnum,
} from '@clients/documentmanager';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Card from 'primevue/card';
import { defineComponent, inject } from 'vue';

export default defineComponent({
  name: 'CompanyCockpitPage',

  components: {
    DocumentDownloadLink,
    ClaimOwnershipPanel,
    CompanyInfoSheet,
    FrameworkSummaryPanel,
    PrimeButton,
    TheContent,
    TheHeader,
    TheFooter,
    Card,
  },

  props: {
    companyId: {
      type: String,
      required: true,
    },
  },

  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      authenticated: inject<boolean>('authenticated'),
      companyRoleAssignments: inject<Array<CompanyRoleAssignmentExtended>>('companyRoleAssignments'),
      injectedUseMobileView: inject<boolean>('useMobileView'),
    };
  },

  data() {
    const latestDocuments: Record<string, DocumentMetaInfoResponse[]> = {};
    Object.keys(DocumentMetaInfoDocumentCategoryEnum).forEach((key) => {
      latestDocuments[`latest${key}`] = [];
    });
    return {
      aggregatedFrameworkDataSummary: undefined as
        | { [key in DataTypeEnum]: AggregatedFrameworkDataSummary }
        | undefined,
      FRAMEWORKS_ALL: ALL_FRAMEWORKS_IN_DISPLAYED_ORDER,
      FRAMEWORKS_MAIN: MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER,
      DocumentMetaInfoDocumentCategoryEnum,
      isUserCompanyOwnerOrUploader: false,
      isUserKeycloakUploader: false,
      isAnyCompanyOwnerExisting: false,
      hasUserAnyRoleInCompany: false,
      showAllFrameworks: false,
      latestDocuments,
      chunkSize: 3,
    };
  },

  computed: {
    useMobileView() {
      return this.injectedUseMobileView;
    },
    isClaimPanelVisible() {
      return !this.isAnyCompanyOwnerExisting && isCompanyIdValid(this.companyId);
    },
    frameworksToDisplay() {
      return this.showAllFrameworks ? this.FRAMEWORKS_ALL : this.FRAMEWORKS_MAIN;
    },
  },

  watch: {
    async companyId(newCompanyId, oldCompanyId) {
      if (newCompanyId !== oldCompanyId) {
        try {
          await this.getAggregatedFrameworkDataSummary();
          await this.getMetaInfoForLatestDocuments();
          await this.setUserRights();
        } catch (error) {
          console.error('Error fetching data for new company:', error);
        }
      }
    },
    async authenticated() {
      await this.setUserRights();
    },
  },

  created() {
    void this.setUserRights();
  },

  mounted() {
    void this.getAggregatedFrameworkDataSummary();
    void this.getMetaInfoForLatestDocuments();
  },

  methods: {
    documentPublicationDateOrEmpty,
    documentNameOrId,
    getPluralCategory,

    /**
     * Retrieves the aggregated framework data summary
     */
    async getAggregatedFrameworkDataSummary(): Promise<void> {
      const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).backendClients
        .companyDataController;
      this.aggregatedFrameworkDataSummary = (
        await companyDataControllerApi.getAggregatedFrameworkDataSummary(this.companyId)
      ).data as { [key in DataTypeEnum]: AggregatedFrameworkDataSummary } | undefined;
    },

    /**
     * Retrieves the latest documents metadata
     */
    async getMetaInfoForLatestDocuments() {
      try {
        const documentControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .documentController;
        for (const value of Object.values(SearchForDocumentMetaInformationDocumentCategoriesEnum)) {
          const categorySet = new Set<SearchForDocumentMetaInformationDocumentCategoriesEnum>([value]);
          const metaInformation = await documentControllerApi.searchForDocumentMetaInformation(
            this.companyId,
            categorySet,
            undefined,
            this.chunkSize
          );
          this.latestDocuments[`latest${value}`] = metaInformation.data;
        }
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * get document categories
     */
    getDocumentData(category: keyof typeof DocumentMetaInfoDocumentCategoryEnum) {
      const key = `latest${category}`;
      return this.latestDocuments[key] || [];
    },

    /**
     * Sends the user to the documents overview
     */
    routeToDocuments(): void {
      void this.$router.push({ path: `/companies/${this.companyId}/documents` });
    },

    /**
     * Checks if the user is allowed to upload datasets for the framework
     * @param framework to check for
     * @returns a boolean as the result of this check
     */
    isUserAllowedToUploadForFramework(framework: DataTypeEnum): boolean {
      return this.isUserCompanyOwnerOrUploader || (isFrameworkPublic(framework) && this.isUserKeycloakUploader);
    },

    /**
     * Set user access rights
     */
    async setUserRights() {
      this.isAnyCompanyOwnerExisting = await hasCompanyAtLeastOneCompanyOwner(this.companyId, this.getKeycloakPromise);
      const companyRoleAssignmentsOfUser = this.companyRoleAssignments;
      if (companyRoleAssignmentsOfUser) {
        const companyRolesForCompanyId = companyRoleAssignmentsOfUser
          .filter((it) => it.companyId === this.companyId)
          .map((it) => it.companyRole);
        this.hasUserAnyRoleInCompany = companyRolesForCompanyId.length > 0;
        this.isUserCompanyOwnerOrUploader =
          companyRolesForCompanyId.includes(CompanyRole.CompanyOwner) ||
          companyRolesForCompanyId.includes(CompanyRole.DataUploader);
      }
      this.isUserKeycloakUploader = await checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise);
    },

    /**
     * Expands or collapses the framework tiles
     */
    toggleShowAll() {
      this.showAllFrameworks = !this.showAllFrameworks;
    },
  },
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
