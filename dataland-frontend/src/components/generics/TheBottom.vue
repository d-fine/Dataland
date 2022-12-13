<template>
  <div class="d-header m-0 fixed bottom-0 surface-900 h-4rem w-full grid align-items-center">
    <div class="col-1 col-offset-10">
      <PrimeButton
        label="Reset"
        class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center surface-900 w-6rem"
        name="reset_request_button"
      >
        Reset
      </PrimeButton>
    </div>
    <div class="col-1">
      <PrimeButton
        label="Submit"
        class="uppercase p-button p-button-sm d-letters text-white d-button justify-content-center bg-primary w-6rem mr-9"
        name="submit_request_button"
      />
    </div>
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import {ApiClientProvider} from "@/services/ApiClients";
import {assertDefined} from "@/utils/TypeScriptUtils";
import {CompanyInformation} from "@clients/backend";
import {inject} from "vue";
import Keycloak from "keycloak-js";

export default {
  name: "TheBottom",
  components: { PrimeButton },
};/*
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data: () => ({
    postInvitation: false,
  }),
  methods: {
    async submitInvitation(event) {
      const filesToUpload: Array<File> = event.files
      try {
        const fileControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)()
        ).getFileControllerApi();
        const response = await fileControllerApi.submitInvitation(filesToUpload);
        this.uploadedFiles.push(...filesToUpload);
      } catch (error) {
        console.error(error);
      } finally {
        this.uploadInProgress = false
        this.uploadFinished = true
      }
    },
  },
}*/
</script>
