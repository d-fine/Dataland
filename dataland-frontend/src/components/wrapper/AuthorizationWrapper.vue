<template>
  <div v-if="hasUserUploaderRights">
    <slot></slot>
  </div>
  <TheContent v-else class="paper-section flex">
    <MiddleCenterDiv class="col-12">
      <div class="col-6 md:col-8 lg:col-12">
        <h1>You can not visit this site because you have no uploader status.</h1>
      </div>
    </MiddleCenterDiv>
  </TheContent>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { checkIfUserHasRole, KEYCLOAK_ROLE_UPLOADER } from "@/utils/KeycloakUtils";
import TheContent from "@/components/generics/TheContent.vue";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";

export default defineComponent({
  name: "AuthorizationWrapper",
  components: { TheContent, MiddleCenterDiv },
  data() {
    return {
      hasUserUploaderRights: null as boolean | null,
    };
  },
  props: {
    requiredRole: {
      type: String,
      required: true,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  mounted: function () {
    checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise)
      .then((hasUserUploaderRights) => {
        this.hasUserUploaderRights = hasUserUploaderRights;
      })
      .catch((error) => console.log(error));
  },
});
</script>
