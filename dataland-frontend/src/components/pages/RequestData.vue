<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="min-h-screen paper-section">
      <div class="pl-4 col-5 text-left">
        <h3 class="py-4">Request companies data for frameworks available on Dataland.</h3>
      </div>
      <div class="m-0 fixed bottom-0 surface-900 h-4rem w-full align-items-center">
        <div class="flex justify-content-end flex-wrap">
          <div class="flex align-items-center justify-content-center m-2">
            <PrimeButton
              label="Submit"
              class="uppercase p-button p-button-sm d-letters justify-content-center w-6rem mr-3"
              name="submit_request_button"
              @click="handleSubmission"
              :disabled="!isFormFilledCorrect"
            >
              Submit
            </PrimeButton>
          </div>
        </div>
      </div>
    </TheContent>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
// import type Keycloak from "keycloak-js"; TODO
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheFooter from "@/components/generics/TheFooter.vue";

export default defineComponent({
  name: "RequestData",
  components: {
    AuthenticationWrapper,
    PrimeButton,
    TheHeader,
    TheContent,
    TheFooter,
  },
  /*
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },*/ // TODO commented out for now, but required later when a POST request is sent
  data() {
    return {
      // submissionFinished: false, TODO
      // submissionInProgress: false, TODO
      isFormFilledCorrect: false, // TODO will adjust based on if the form is filled correctly (similar to upload page)
    };
  },

  methods: {
    /*

    async handleSubmission() {
      this.submissionInProgress = true;
    // TODO: some POST request is sent
      this.submissionFinished = true;
      this.submissionInProgress = false;
    }, */
    // TODO commented out for now, but required later when a POST request is sent
  },
});
</script>
