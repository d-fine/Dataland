<template>
  <PrimeButton
    class="uppercase p-button-outlined p-button-sm mr-3"
    @click="setQaStatusTo($event, QaStatus.Rejected)"
    :disabled="reviewSubmitted"
  >
    <i class="material-icons"> clear </i>
    <span class="d-letters pl-2"> Reject </span>
  </PrimeButton>
  <PrimeButton
    class="uppercase p-button p-button-sm"
    @click="setQaStatusTo($event, QaStatus.Accepted)"
    :disabled="reviewSubmitted"
  >
    <i class="material-icons"> done </i>
    <span class="d-letters pl-2"> Approve </span>
  </PrimeButton>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import PrimeButton from "primevue/button";
import type Keycloak from "keycloak-js";
import { QaStatus } from "@clients/qaservice";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "QualityAssuranceButtons",
  components: { PrimeButton },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      reviewSubmitted: false,
      reviewSuccessful: false,
      QaStatus,
    };
  },
  props: {
    dataId: { type: String, required: true },
  },
  methods: {
    /**
     * Sets dataset quality status to the given status
     * @param event the click event
     * @param qaStatus the QA status to be assigned
     */
    async setQaStatusTo(event: MouseEvent, qaStatus: QaStatus) {
      event.preventDefault();
      event.stopImmediatePropagation();

      try {
        this.reviewSubmitted = true;
        const qaServiceControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getQaControllerApi();
        // await qaServiceControllerApi.assignQaStatus(this.dataId, qaStatus);
        this.reviewSuccessful = true;
        console.log(qaStatus, "success");
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
