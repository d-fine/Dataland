<template>
  <div v-if="isVisible">
    <PrimeButton
      class="uppercase p-button-outlined p-button p-button-sm d-letters ml-3"
      aria-label="CLOSE REQUEST"
      @click="closeRequest"
      data-test="editDatasetButton"
    >
      <span class="px-2">CLOSE REQUEST</span>
      <span class="material-icons-outlined">arrow_drop_down</span>
    </PrimeButton>
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
import { inject } from "vue";
import type Keycloak from "keycloak-js";
import { getAnsweredDataRequestsForViewPage } from "@/utils/ReqeustUtils";

export default defineComponent({
  name: "ReviewRequestButtons",
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  components: { PrimeButton },
  props: {
    isVisible: {
      default: false,
    },
    companyId: {
      type: String,
      default: "testcompanyID",
    },
    framework: {
      type: String,
      default: "testframework",
    },
  },
  methods: {
    async closeRequest() {
      console.log(" here I will close the request #todo");
      const listOFMyRequests = await getAnsweredDataRequestsForViewPage(
        this.companyId,
        this.framework,
        this.getKeycloakPromise,
      );
      console.log(listOFMyRequests);
    },
  },
});
</script>
