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
          <div class="p-badge badge-light-green outline" data-test="verifiedDataOwnerBadge" v-if="hasCompanyDataOwner">
            <span class="material-icons-outlined fs-sm">verified</span>
            Verified Data Owner
          </div>
        </div>
        <div class="right-elements">
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
          <span class="font-semibold">{{ displaySector }}</span>
        </div>
        <div class="company-details__info">
          <span>Headquarter: </span>
          <span class="font-semibold">{{ companyInformation.headquarters }}</span>
        </div>
        <div class="company-details__info">
          <span>ISIN: </span>
          <span class="font-semibold">{{ displayIsin }}</span>
        </div>
      </div>
    </div>
    <div v-else-if="companyIdDoesNotExist" class="col-12">
      <h1 class="mb-0" data-test="noCompanyWithThisIdErrorIndicator">No company with this ID present</h1>
    </div>
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { defineComponent, inject } from "vue";
import { type CompanyInformation, IdentifierType } from "@clients/backend";
import type Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import ContextMenuButton from "@/components/general/ContextMenuButton.vue";
import ClaimOwnershipDialog from "@/components/resources/companyCockpit/ClaimOwnershipDialog.vue";
import { getUserId } from "@/utils/KeycloakUtils";
import { getErrorMessage } from "@/utils/ErrorMessageUtils";
import SingleDataRequestButton from "@/components/resources/companyCockpit/SingleDataRequestButton.vue";
import { getCompanyDataOwnerInformation } from "@/utils/api/CompanyDataOwner";
import { isCompanyIdValid } from "@/utils/ValidationsUtils";

export default defineComponent({
  name: "CompanyInformation",
  components: { ClaimOwnershipDialog, ContextMenuButton, SingleDataRequestButton },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  emits: ["fetchedCompanyInformation"],
  data() {
    return {
      companyInformation: null as CompanyInformation | null,
      waitingForData: true,
      companyIdDoesNotExist: false,
      isUserDataOwner: false,
      hasCompanyDataOwner: false,
      dialogIsOpen: false,
      claimIsSubmitted: false,
      userId: undefined as string | undefined,
    };
  },
  computed: {
    displaySector() {
      if (this.companyInformation?.sector) {
        return this.companyInformation?.sector;
      } else {
        return "—";
      }
    },
    displayIsin() {
      return this.companyInformation?.identifiers?.[IdentifierType.Isin]?.[0] ?? "—";
    },
    contextMenuItems() {
      const listOfItems = [];
      if (!this.isUserDataOwner && this.userId !== undefined) {
        listOfItems.push({
          label: "Claim Company Dataset Ownership",
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
  },
  mounted() {
    void this.getCompanyInformation();
    this.hasCompanyDataOwner = await getCompanyDataOwnerInformation(
      assertDefined(this.getKeycloakPromise),
      this.companyId,
    );
    void this.awaitUserId();
    void this.getUserDataOwnerInformation();
  },
  watch: {
    async companyId(newCompanyId, oldCompanyId) {
      if (newCompanyId !== oldCompanyId) {
        try {
          await this.getCompanyInformation();
          this.hasCompanyDataOwner = await getCompanyDataOwnerInformation(
            assertDefined(this.getKeycloakPromise),
            newCompanyId as string,
          );
          await this.getUserDataOwnerInformation();
          this.claimIsSubmitted = false;
        } catch (error) {
          console.error("Error fetching data for new company:", error);
        }
      }
    },
  },
  methods: {
    /**
     * handles the close button click event of the dialog
     */
    onCloseDialog() {
      this.dialogIsOpen = false;
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
          this.waitingForData = false;
          this.$emit("fetchedCompanyInformation", this.companyInformation);
        }
      } catch (error) {
        console.error(error);
        if (getErrorMessage(error).includes("404")) {
          this.companyIdDoesNotExist = true;
        }
        this.waitingForData = false;
        this.companyInformation = null;
      }
    },

    /**
     * Get the Information about Data-ownership
     */
    async getUserDataOwnerInformation() {
      await this.awaitUserId();
      if (this.userId !== undefined && isCompanyIdValid(this.companyId)) {
        try {
          const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)())
            .backendClients.companyDataController;
          const axiosResponse = await companyDataControllerApi.isUserDataOwnerForCompany(
            this.companyId,
            assertDefined(this.userId),
          );
          if (axiosResponse.status == 200) {
            this.isUserDataOwner = true;
          }
        } catch (error) {
          console.log(error);
          if (getErrorMessage(error).includes("404")) {
            this.isUserDataOwner = false;
          }
        }
      } else {
        this.isUserDataOwner = false;
      }
    },
    /**
     * handles the emitted claim event
     */
    onClaimSubmitted() {
      this.claimIsSubmitted = true;
    },
    /**
     * gets the user ID in an async manner
     */
    async awaitUserId(): Promise<void> {
      this.userId = await getUserId(assertDefined(this.getKeycloakPromise));
    },
  },
});
</script>

<style scoped lang="scss">
.inline-loading {
  width: 450px;
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
