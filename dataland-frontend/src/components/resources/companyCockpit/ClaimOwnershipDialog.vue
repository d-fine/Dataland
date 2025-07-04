<template>
  <PrimeDialog
    id="claimOwnerShipDialog"
    :dismissable-mask="true"
    :modal="true"
    header="Header"
    :closable="!claimIsSubmitted"
    footer="Footer"
    class="col-6"
    v-model:visible="dialogIsVisible"
  >
    <template #header>
      <h2 v-if="!claimIsSubmitted" class="m-0">Claim ownership for your company.</h2>
      <h2 v-else class="m-0">Thank you for claiming company ownership for {{ companyName }}.</h2>
    </template>

    <div v-if="!claimIsSubmitted">
      <p data-test="claimOwnershipDialogMessage">
        Are you responsible for the datasets of {{ companyName }}? Claim company ownership in order to ensure high
        quality and transparency over your company's data.
      </p>
      <p>Feel free to share any additional information with us:</p>

      <FormKit
        v-model="claimOwnershipMessage"
        type="textarea"
        name="claimOwnershipMessage"
        placeholder="Write your message."
        data-test="messageInputField"
        wrapper-class="full-width-wrapper"
        input-class="textarea"
        inner-class="no-shadow"
      />
    </div>
    <div v-else>
      <p data-test="claimOwnershipDialogSubmittedMessage">We will reach out to you soon via email.</p>
    </div>
    <template #footer v-if="!claimIsSubmitted">
      <PrimeButton class="w-full" label="SUBMIT" @click="submitInput" data-test="submitButton" />
    </template>
    <template #footer v-else>
      <PrimeButton class="p-button p-button-outlined" label="CLOSE" @click="closeDialog" data-test="closeButton" />
    </template>
  </PrimeDialog>
</template>

<script lang="ts">
import PrimeDialog from 'primevue/dialog';
import PrimeButton from 'primevue/button';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { inject, defineComponent } from 'vue';
import type Keycloak from 'keycloak-js';
import { FormKit } from '@formkit/vue';

export default defineComponent({
  name: 'ClaimOwnershipDialog',
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  components: {
    PrimeDialog,
    PrimeButton,
    FormKit,
  },
  data() {
    return {
      claimOwnershipMessage: '',
      dialogIsVisible: false,
    };
  },
  props: {
    dialogIsOpen: {
      type: Boolean,
      required: false,
      default: false,
    },
    companyName: {
      type: String,
      required: true,
    },
    companyId: {
      type: String,
      required: true,
    },
    claimIsSubmitted: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  emits: ['claimSubmitted', 'closeDialog'],
  methods: {
    /**
     * Makes the API request in order to post the request for company ownership
     */
    async submitInput(): Promise<void> {
      const companyRolesControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
        .companyRolesController;
      try {
        const axiosResponse = await companyRolesControllerApi.postCompanyOwnershipRequest(
          this.companyId,
          this.claimOwnershipMessage ? this.claimOwnershipMessage : undefined
        );
        if (axiosResponse.status == 200) {
          this.$emit('claimSubmitted');
        }
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * closes the dialog window and emits this event
     */
    closeDialog(): void {
      this.dialogIsVisible = false;
      this.$emit('closeDialog');
    },
  },
  watch: {
    dialogIsOpen(newValue: boolean): void {
      this.dialogIsVisible = newValue;
    },
  },
});
</script>

<style lang="scss">
.full-width-wrapper {
  max-width: 100%;
}

.textarea {
  background-color: var(--gray-200);
}

.no-shadow {
  box-shadow: none;
}
</style>
