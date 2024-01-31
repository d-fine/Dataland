<template>
  <div class="claim-panel">
    <div class="next-to-each-other vertical-middle">
      <h2 class="claim-panel__ownership-question">Responsible for {{ companyName }}?</h2>
      <a class="link" @click="toggleDialog">Claim company dataset ownership.</a>
    </div>
  </div>
  <ClaimOwnershipDialog
      :dialog-is-open="dialogIsOpen"
      :company-name="companyName"
      :company-id="companyId"
      :claim-is-submitted="claimIsSubmitted"
      @claim-submitted="onClaimSubmitted"
      @close-dialog="onCloseDialog"
  />
</template>

<script lang="ts">
import {defineComponent, inject} from "vue";
import ClaimOwnershipDialog from "@/components/resources/companyCockpit/ClaimOwnershipDialog.vue";
import {ApiClientProvider} from "@/services/ApiClients";
import {assertDefined} from "@/utils/TypeScriptUtils";
import type Keycloak from "keycloak-js";

export default defineComponent({
  name: "CompanyCockpitPage",
  components: {
    ClaimOwnershipDialog,
  },
  inject: {
    injectedUseMobileView: {
      from: "useMobileView",
      default: false,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      dialogIsOpen: false,
      claimIsSubmitted: false,
      companyName: "",
    };
  },
  watch: {
    companyId(newCompanyId, oldCompanyId) {
      if (newCompanyId !== oldCompanyId) {
        this.dialogIsOpen = false;
        this.claimIsSubmitted = false;
        void this.getCompanyName();
      }
    },
  },
  props: {
    companyId: {
      type: String,
      required: true,
    },
  },
  mounted() {
    void this.getCompanyName();
  },
  methods: {
    /**
     * handles the emitted claim event
     */
    onClaimSubmitted() {
      this.claimIsSubmitted = true;
    },
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
    async getCompanyName() {
      try {
        if (this.companyId !== undefined) {
          const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)())
              .backendClients.companyDataController;
          this.companyName = (await companyDataControllerApi.getCompanyInfo(this.companyId)).data.companyName;
        }
      } catch (error) {
        console.error(error);
      }
    },
    toggleDialog() {
      this.dialogIsOpen = true;
    },
  },
});
</script>

<style scoped lang="scss">
.claim-panel {
  grid-column: 1 / -1;
  min-height: 100px;
  background-color: var(--surface-card);
  padding: $spacing-md;
  border-radius: $radius-xxs;
  text-align: left;
  box-shadow: 0 0 12px var(--gray-300);

  &__ownership-question {
    font-size: 21px;
    font-weight: 700;
  }
}
</style>
