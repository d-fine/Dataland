<template>
  <TheHeader v-if="!useMobileView" />
  <TheContent class="paper-section flex">
    <CompanyInfoSheet :company-id="companyId" :show-single-data-request-button="true" />
    <div class="grid-container">
      <div class="paper-section">
        <div class="card">
          <div class="card__title">Latest Documents</div>
          <div class="card__separator" />
          <div v-for="(category, label) in DocumentMetaInfoDocumentCategoryEnum" :key="category" :data-test="category">
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
          <a :href="`/companies/${companyId}/documents`" class="tertiary-button">
            VIEW ALL DOCUMENTS <span class="material-icons">arrow_forward_ios</span>
          </a>
        </div>
      </div>
      <div>
        <div class="card-grid" :data-test="'summaryPanels'">
          <ClaimOwnershipPanel v-if="isClaimPanelVisible" :company-id="companyId" />
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

        <div
          class="document-button cursor-pointer flex flex-row align-items-center justify-content-end"
          @click="toggleShowAll"
          style="margin-left: auto"
        >
          <span class="text-primary font-semibold d-letters" :data-test="'toggleShowAll'">
            {{ showAllFrameworks ? 'SHOW LESS' : 'SHOW ALL' }}
          </span>
          <i class="material-icons text-primary">
            {{ showAllFrameworks ? 'expand_less' : 'expand_more' }}
          </i>
        </div>
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
import { CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';
import {
  DocumentMetaInfoDocumentCategoryEnum,
  type DocumentMetaInfoResponse,
  SearchForDocumentMetaInformationDocumentCategoriesEnum,
} from '@clients/documentmanager';
import type Keycloak from 'keycloak-js';
import { defineComponent, inject } from 'vue';

export default defineComponent({
  name: 'CompanyCockpitPage',

  components: {
    DocumentDownloadLink,
    ClaimOwnershipPanel,
    CompanyInfoSheet,
    FrameworkSummaryPanel,
    TheContent,
    TheHeader,
    TheFooter,
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
      companyRoleAssignments: inject<Array<CompanyRoleAssignment>>('companyRoleAssignments'),
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
     * Checks if the user is allowed to upload datasets for the framework
     * @param framework to check for
     * @returns a boolean as result of this check
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
@use '@/assets/scss/newVariables';

.card-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  padding-top: 40px;
  padding-bottom: 40px;
  @media only screen and (max-width: newVariables.$small) {
    padding: 24px 17px;
  }
}

.grid-container {
  display: grid;
  grid-template-columns: 3fr 6fr 30px;
  padding: 40px;
  gap: 40px;
  @media only screen and (max-width: newVariables.$small) {
    width: 100%;
    grid-template-columns: repeat(1, 1fr);
    padding: 24px 3%;
  }
}

.card-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 40px;
  flex-wrap: wrap;
  justify-content: space-between;
  @media only screen and (max-width: newVariables.$medium) {
    grid-template-columns: repeat(2, 1fr);
  }
  @media only screen and (max-width: newVariables.$small) {
    width: 100%;
    grid-template-columns: repeat(1, 1fr);
  }
}

.card {
  width: 90%;
  background-color: var(--surface-card);
  padding: 40px;
  margin: 0 20px 1rem 40px;
  box-shadow: 0 0 12px var(--gray-300);
  border-radius: 0.5rem;
  text-align: left;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  @media only screen and (max-width: newVariables.$small) {
    width: 100%;
    margin-left: 0;
    margin-right: 0;
    padding: 20px;
  }

  &__title {
    font-size: 21px;
    font-weight: 700;
    line-height: 27px;
  }

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

.document-button {
  width: fit-content;
  margin-top: 20px;
  @media only screen {
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
  }
}
</style>
