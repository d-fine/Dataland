<template>
  <PrimeDialog
      id="claimOwnerShipDialog"
      :dismissable-mask="true"
      :modal="true"
      header="Header"
      footer="Footer"
      class="col-6"
      v-model:visible="dialogIsVisible"
  >
    <template #header>
      <h2 v-if="!claimIsSubmitted" class="m-0">Claim dataset ownership for your company.</h2>
      <h2 v-else class="m-0">Thank you for claiming data ownership for {{ companyName }}.</h2>
    </template>

    <div v-if="!claimIsSubmitted">
      <p>
        Are you responsible for the datasets of {{ companyName }}? Claim dataset ownership in order to ensure high
        quality and transparency over your company's data.
      </p>
      <p>Feel free to share any additional information with us:</p>
      <FormKit
          v-model="claimOwnershipMessage"
          type="textarea"
          name="claimOwnershipMessage"
          placeholder="Write your message."
          class="w-full p-inputtext"
      />
    </div>
    <div v-else>
      <p>We will reach out to you soon via email.</p>
    </div>
    <template #footer v-if="!claimIsSubmitted">
      <PrimeButton label="SUBMIT" @click="submitInput"/>
    </template>
    <template #footer v-else>
      <PrimeButton label="CLOSE" @click="$emit('toggleDialog')"/>
    </template>
  </PrimeDialog>
</template>

<script lang="ts">
import PrimeDialog from "primevue/dialog";
import PrimeButton from "primevue/button";
import {ApiClientProvider} from "@/services/ApiClients";
import {assertDefined} from "@/utils/TypeScriptUtils";
import {inject, defineComponent} from "vue";
import type Keycloak from "keycloak-js";

export default defineComponent({
  name: "ClaimOwnershipDialog",
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  components: {
    PrimeDialog,
    PrimeButton,
  },
  data() {
    return {
      claimIsSubmitted: false,
      claimOwnershipMessage: "",
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
  },
  emits: ["toggleDialog"],
  methods: {
    /**
     * Makes the API request in order to post the request for data ownership
     */
    async submitInput(): void {
      const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).backendClients
          .companyDataController;
      try {
        const axiosResponse = await companyDataControllerApi.postDataOwnershipRequest(
            this.companyId,
            this.claimOwnershipMessage ? this.claimOwnershipMessage : undefined,
        );
        if (axiosResponse.status == 200) {
          this.claimIsSubmitted = true;
        }
      } catch (error) {
        console.error(error);
      }
    },
  },
  watch: {
    dialogIsOpen(newValue: boolean): void {
      this.dialogIsVisible = newValue;
    },
  },
});
</script>