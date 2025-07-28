<template>
  <Card
    :pt="{
      body: {
        style: {
          padding: 'var(--spacing-sm) var(--spacing-lg)',
          gap: 'var(--spacing-none)',
        },
      },
    }"
  >
    <template #title>
      <span data-test="claimOwnershipPanelHeading">Responsible for {{ companyName }}?</span>
      <PrimeButton
        data-test="claimOwnershipPanelLink"
        label="Claim company ownership."
        variant="link"
        @click="openDialog"
        :pt="{
          label: {
            style: {
              fontSize: 'var(--font-size-lg)',
              fontWeight: 'var(--font-weight-bold)',
            },
          },
        }"
      />
    </template>
  </Card>
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
import { defineComponent, inject } from 'vue';
import ClaimOwnershipDialog from '@/components/resources/companyCockpit/ClaimOwnershipDialog.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import Card from 'primevue/card';
import PrimeButton from 'primevue/button';

export default defineComponent({
  name: 'CompanyCockpitPage',
  components: {
    ClaimOwnershipDialog,
    Card,
    PrimeButton,
  },
  inject: {
    injectedUseMobileView: {
      from: 'useMobileView',
      default: false,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data() {
    return {
      dialogIsOpen: false,
      claimIsSubmitted: false,
      companyName: '',
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
    /**
     * opens the dialog
     */
    async openDialog() {
      const isLoggedIn = await this.checkLoggedIn();
      if (isLoggedIn) {
        this.dialogIsOpen = true;
      } else {
        const keycloakInstance = await assertDefined(this.getKeycloakPromise)();
        await keycloakInstance.register();
      }
    },
    /**
     * checks if user is logged in
     * @returns boolean
     */
    async checkLoggedIn() {
      if (this.getKeycloakPromise) {
        return (await this.getKeycloakPromise()).authenticated;
      }
      return false;
    },
  },
});
</script>
