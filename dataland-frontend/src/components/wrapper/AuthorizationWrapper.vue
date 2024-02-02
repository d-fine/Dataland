<template>
  <div v-if="hasUserRequiredRole || isUserDataOwner">
    <slot></slot>
  </div>
  <TheContent v-else class="paper-section flex">
    <MiddleCenterDiv class="col-12">
      <div class="col-6 md:col-8 lg:col-12">
        <h1>You do not have permission to visit this page.</h1>
      </div>
    </MiddleCenterDiv>
  </TheContent>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import type Keycloak from "keycloak-js";
import { checkIfUserHasRole } from "@/utils/KeycloakUtils";
import { isUserDataOwnerForCompany } from "@/utils/DataOwnerUtils";
import TheContent from "@/components/generics/TheContent.vue";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";

export default defineComponent({
  name: "AuthorizationWrapper",
  components: { TheContent, MiddleCenterDiv },
  data() {
    return {
      hasUserRequiredRole: null as boolean | null,
      isUserDataOwner: null as boolean | null,
    };
  },
  props: {
    requiredRole: {
      type: String,
      required: true,
    },
    companyId: String,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  mounted: function () {
    checkIfUserHasRole(this.requiredRole, this.getKeycloakPromise)
      .then((hasUserRequiredRole) => {
        this.hasUserRequiredRole = hasUserRequiredRole;
      })
      .catch((error) => console.log(error));
    isUserDataOwnerForCompany(this.companyId, this.getKeycloakPromise)
      .then((isUserDataOwner) => {
        this.isUserDataOwner = isUserDataOwner;
      })
      .catch((error) => console.log(error));
  },
});
</script>
