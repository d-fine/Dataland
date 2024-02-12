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
          <div class="p-badge badge-light-green outline" data-test="verifiedDataOwnerBadge" v-if="isUserDataOwner">
            <span class="material-icons-outlined fs-sm">verified</span>
            Verified Data Owner
          </div>
        </div>
        <div>
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
import { getErrorMessage } from "@/utils/ErrorMessageUtils";
import { isUserDataOwnerForCompany } from "@/utils/DataOwnerUtils";

export default defineComponent({
  name: "CompanyInformation",
  components: { ClaimOwnershipDialog, ContextMenuButton },
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
      if (!this.isUserDataOwner && this.userId) {
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
  },
  mounted() {
    void this.getCompanyInformation();
    void this.setDataOwnershipStatus();
  },
  watch: {
    companyId() {
      void this.getCompanyInformation();
      void this.setDataOwnershipStatus();
      this.claimIsSubmitted = false;
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
     * Set the data-ownership status of current user
     * @returns a void promise so that the setter-function can be awaited
     */
    async setDataOwnershipStatus(): Promise<void> {
      return isUserDataOwnerForCompany(this.companyId, this.getKeycloakPromise).then((result) => {
        this.isUserDataOwner = result;
      });
    },
    /**
     * handles the emitted claim event
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

.left-elements {
  display: flex;
  align-items: center;
}

.fs-sm {
  font-size: $fs-sm;
  margin-right: 0.25rem;
}
</style>
