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
                <DocumentLink
                  :download-name="
                    truncatedDocumentName(document.documentName ? document.documentName : document.documentId) +
                    ' (' +
                    document.publicationDate +
                    ')'
                  "
                  :file-reference="document.documentId"
                  show-icon
                />
              </div>
            </div>
          </div>
          <div class="p-col-12 text-right">
            <div class="document-button cursor-pointer flex flex-row align-items-center" @click="goToDocumentOverview">
              <span class="text-primary font-semibold d-letters"> VIEW ALL DOCUMENTS</span>
              <span class="material-icons text-primary">arrow_forward_ios</span>
            </div>
          </div>
        </div>
      </div>
      <div>
        <div class="card-grid" :data-test="'summaryPanels'">
          <ClaimOwnershipPanel v-if="isClaimPanelVisible" :company-id="companyId" />
          <FrameworkSummaryPanel
            v-for="framework of frameworksToDisplay"
            :key="framework"
            :is-user-allowed-to-view="authenticated === true"
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
  <TheFooter :is-light-version="true" :sections="footerContent" />
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheContent from '@/components/generics/TheContent.vue';
import { type AggregatedFrameworkDataSummary, type DataTypeEnum } from '@clients/backend';
import { ApiClientProvider } from '@/services/ApiClients';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import type Keycloak from 'keycloak-js';
import FrameworkSummaryPanel from '@/components/resources/companyCockpit/FrameworkSummaryPanel.vue';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import { FRAMEWORKS_ALL, FRAMEWORKS_MAIN } from '@/utils/Constants';
import ClaimOwnershipPanel from '@/components/resources/companyCockpit/ClaimOwnershipPanel.vue';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { hasCompanyAtLeastOneCompanyOwner } from '@/utils/CompanyRolesUtils';
import { isCompanyIdValid } from '@/utils/ValidationUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';
import { isFrameworkPublic } from '@/utils/Frameworks';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import {
  DocumentMetaInfoDocumentCategoryEnum,
  type DocumentMetaInfoResponse,
  SearchForDocumentMetaInformationDocumentCategoriesEnum,
} from '@clients/documentmanager';
import router from '@/router';
import DocumentLink from '@/components/resources/frameworkDataSearch/DocumentLink.vue';
import { getPluralCategory } from '@/utils/StringFormatter';

export default defineComponent({
  name: 'CompanyCockpitPage',
  components: {
    DocumentLink,
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
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    const latestDocuments: Record<string, DocumentMetaInfoResponse[]> = {};
    Object.keys(DocumentMetaInfoDocumentCategoryEnum).forEach((key) => {
      latestDocuments[`latest${key}`] = [];
    });
    return {
      aggregatedFrameworkDataSummary: undefined as
        | { [key in DataTypeEnum]: AggregatedFrameworkDataSummary }
        | undefined,
      FRAMEWORKS_ALL,
      FRAMEWORKS_MAIN,
      DocumentMetaInfoDocumentCategoryEnum,
      isUserCompanyOwnerOrUploader: false,
      isUserKeycloakUploader: false,
      isAnyCompanyOwnerExisting: false,
      hasUserAnyRoleInCompany: false,
      footerContent,
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
    this.getLatestDocuments();
  },
  methods: {
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
    getLatestDocuments(): void {
      try {
        const documentControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .documentController;
        for (const value of Object.values(SearchForDocumentMetaInformationDocumentCategoriesEnum)) {
          const categorySet = new Set<SearchForDocumentMetaInformationDocumentCategoriesEnum>([value]);
          documentControllerApi
            .searchForDocumentMetaInformation(this.companyId, categorySet, undefined, this.chunkSize)
            .then((metaInformation) => (this.latestDocuments[`latest${value}`] = metaInformation.data))
            .catch((error) => console.error(error));
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
    /**
     * routing to DocumentOverview page
     */
    goToDocumentOverview() {
      void router.push(`/companies/${this.companyId}/documents`);
    },
    /**
     * Shorten document names
     */
    truncatedDocumentName(name: string) {
      return name.length > 25 ? name.slice(0, 25) + '...' : name;
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
  grid-template-columns: 5fr 6fr 30px;
  padding: 40px;
  gap: 40px;
  @media only screen and (max-width: newVariables.$small) {
    width: 100%;
    grid-template-columns: repeat(1, 1fr);
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
    font-weight: 400;
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
