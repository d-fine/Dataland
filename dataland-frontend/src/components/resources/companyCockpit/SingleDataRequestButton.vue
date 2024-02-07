<template>
  <PrimeButton
    v-if="isUserAllowed"
    @click="onClick"
    class="uppercase p-button p-button-sm mr-3"
    data-test="singleDataRequestButton"
  >
    <span class="d-letters pl-2"> Request Data </span>
  </PrimeButton>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import PrimeButton from "primevue/button";
import type Keycloak from "keycloak-js";
import { checkIfUserHasRole, KEYCLOAK_ROLE_UPLOADER } from "@/utils/KeycloakUtils";

export default defineComponent({
  name: "SingleDataRequestButton",
  components: { PrimeButton },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  props: {
    companyId: {
      type: String,
      required: true,
    },
  },
  mounted() {
    checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise)
      .then((result) => {
        this.isUserAllowed = result;
      })
      .catch((error) => console.log(error));
  },
  data() {
    return {
      isUserAllowed: false,
    };
  },
  methods: {
    /**
     * navigates to the single data request page
     * @returns a router push
     */
    onClick() {
      const thisCompanyId = this.companyId;
      return this.$router.push({
        path: `/singledatarequest/${thisCompanyId}`,
      });
    },
  },
});
</script>
