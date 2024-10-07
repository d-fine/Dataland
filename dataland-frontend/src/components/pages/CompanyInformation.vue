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
          <ReviewRequestButtons
            v-if="!!framework && !!mapOfReportingPeriodToActiveDataset"
            :map-of-reporting-period-to-active-dataset="mapOfReportingPeriodToActiveDataset"
            :framework="framework"
            :company-id="companyId"
          />
          <SingleDataRequestButton :company-id="companyId" v-if="showSingleDataRequestButton" />
          <ContextMenuButton v-if="contextMenuItems.length > 0" :menu-items="contextMenuItems" />
        </div>
      </div>

      <ClaimOwnershipDialog
        :company-id="companyId"
        :company-name="companyInformation.companyName"
        :dialog-is-open="dialogIsOpen"
        :claim-is-submitted="claimIsSubmitted"
        @claim-submitted="onClaimSubmitted"
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

<script lang="ts">
import { ApiClientProvider } from '@/services/ApiClients';
import { defineComponent, inject, type PropType } from 'vue';
import {
  type CompanyIdAndName,
  type CompanyInformation,
  type DataMetaInformation,
  type DataTypeEnum,
  IdentifierType,
} from '@clients/backend';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import ContextMenuButton from '@/components/general/ContextMenuButton.vue';
import ClaimOwnershipDialog from '@/components/resources/companyCockpit/ClaimOwnershipDialog.vue';
import { getErrorMessage } from '@/utils/ErrorMessageUtils';
import SingleDataRequestButton from '@/components/resources/companyCockpit/SingleDataRequestButton.vue';
import { hasCompanyAtLeastOneCompanyOwner, hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import ReviewRequestButtons from '@/components/resources/dataRequest/ReviewRequestButtons.vue';
import { getCompanyDataForFrameworkDataSearchPageWithoutFilters } from '@/utils/SearchCompaniesForFrameworkDataPageDataRequester';
import { CompanyRole } from '@clients/communitymanager';
import router from '@/router';

export default defineComponent({
  name: 'CompanyInformation',
  components: { ClaimOwnershipDialog, ContextMenuButton, SingleDataRequestButton, ReviewRequestButtons },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      authenticated: inject<boolean>('authenticated'),
    };
  },
  emits: ['fetchedCompanyInformation'],
  data() {
    return {
      companyInformation: null as CompanyInformation | null,
      waitingForData: true,
      companyIdDoesNotExist: false,
      isUserCompanyOwner: false,
      hasCompanyOwner: false,
      dialogIsOpen: false,
      claimIsSubmitted: false,
      hasParentCompany: undefined as boolean | undefined,
      parentCompany: null as CompanyIdAndName | null,
    };
  },
  computed: {
    displaySector() {
      if (this.companyInformation?.sector) {
        return this.companyInformation?.sector;
      } else {
        return '—';
      }
    },
    displayLei() {
      return this.companyInformation?.identifiers?.[IdentifierType.Lei]?.[0] ?? '—';
    },
    contextMenuItems() {
      const listOfItems = [];
      if (!this.isUserCompanyOwner && this.authenticated) {
        listOfItems.push({
          label: 'Claim Company Dataset Ownership',
          command: () => {
            this.dialogIsOpen = true;
          },
        });
      }
      return listOfItems;
    },
  },
  props: {
    companyId: {
      type: String,
      required: true,
    },
    showSingleDataRequestButton: {
      type: Boolean,
      default: false,
    },
    framework: {
      type: String as PropType<DataTypeEnum>,
      required: false,
    },
    mapOfReportingPeriodToActiveDataset: {
      type: Map as PropType<Map<string, DataMetaInformation>>,
      required: false,
    },
  },
  mounted() {
    this.fetchDataForThisPage();
  },
  watch: {
    async companyId() {
      await this.fetchDataForThisPage();
    },
  },
  methods: {
    /**
     * A complete fetch of all data that is relevant for UI elements of this page
     */
    async fetchDataForThisPage() {
      try {
        void this.getCompanyInformation();
        void this.setCompanyOwnershipStatus();
        void this.updateHasCompanyOwner();
        this.claimIsSubmitted = false;
      } catch (error) {
        console.error('Error fetching data for new company:', error);
      }
    },

    /**
     * triggers route push to parent company if the parent company exists
     * @returns route push
     */
    async visitParentCompany() {
      if (this.parentCompany) {
        const parentCompanyUrl = `/companies/${this.parentCompany.companyId}`;
        return router.push(parentCompanyUrl);
      }
    },
    /**
     * Updates the hasCompanyOwner in an async way
     */
    async updateHasCompanyOwner() {
      this.hasCompanyOwner = await hasCompanyAtLeastOneCompanyOwner(this.companyId, this.getKeycloakPromise);
    },
    /**
     * Handles the close button click event of the dialog
     */
    onCloseDialog() {
      this.dialogIsOpen = false;
    },
    /**
     * Gets the parent company based on the lei
     * @param parentCompanyLei lei of the parent company
     */
    async getParentCompany(parentCompanyLei: string) {
      try {
        const companyIdAndNames = await getCompanyDataForFrameworkDataSearchPageWithoutFilters(
          parentCompanyLei,
          assertDefined(this.getKeycloakPromise)(),
          1
        );
        if (companyIdAndNames.length > 0) {
          this.parentCompany = companyIdAndNames[0];
          this.hasParentCompany = true;
        } else {
          this.hasParentCompany = false;
        }
      } catch (e) {
        console.error(e);
      }
    },
    /**
     * Uses the dataland API to retrieve information about the company identified by the local
     * companyId object.
     */
    async getCompanyInformation() {
      try {
        this.waitingForData = true;
        if (this.companyId !== undefined) {
          const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)())
            .backendClients.companyDataController;
          this.companyInformation = (await companyDataControllerApi.getCompanyInfo(this.companyId)).data;
          if (this.companyInformation.parentCompanyLei != null) {
            this.getParentCompany(this.companyInformation.parentCompanyLei).catch(() => {
              console.error(`Unable to find company with LEI: ${this.companyInformation?.parentCompanyLei}`);
            });
          } else {
            this.hasParentCompany = false;
          }
          this.waitingForData = false;
          this.$emit('fetchedCompanyInformation', this.companyInformation);
        }
      } catch (error) {
        console.error(error);
        if (getErrorMessage(error).includes('404')) {
          this.companyIdDoesNotExist = true;
        }
        this.waitingForData = false;
        this.companyInformation = null;
      }
    },

    /**
     * Set the company-ownership status of current user
     * @returns a void promise so that the setter-function can be awaited
     */
    async setCompanyOwnershipStatus(): Promise<void> {
      return hasUserCompanyRoleForCompany(CompanyRole.CompanyOwner, this.companyId, this.getKeycloakPromise).then(
        (result) => {
          this.isUserCompanyOwner = result;
        }
      );
    },
    /**
     * Handles the emitted claim event
     */
    onClaimSubmitted() {
      this.claimIsSubmitted = true;
    },
  },
});
</script>

<style scoped lang="scss">
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
    @media only screen and (max-width: $small) {
      width: 100%;
      border-bottom: #e0dfde 1px solid;
      margin-bottom: 0.5rem;
    }
  }

  &__info-holder {
    display: flex;
    flex-direction: row;
    @media only screen and (max-width: $small) {
      flex-direction: column;
    }
  }

  &__info {
    padding-top: 0.3rem;
    @media only screen and (min-width: $small) {
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
  font-size: $fs-sm;
  margin-right: 0.25rem;
}
</style>
