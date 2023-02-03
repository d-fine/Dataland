<template>
  <div v-if="hasUserUploaderRights">
    <slot></slot>
  </div>
  <div v-else>
    <TheContent>
      <h1>You can not visit this site because you have no uploader status.</h1>
    </TheContent>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { checkIfUserHasUploaderRights } from "@/utils/KeycloakUtils";
import TheContent from "@/components/generics/TheContent.vue";

export default defineComponent({
  name: "UploaderRoleWrapper",
  components: { TheContent },
  data() {
    return {
      hasUserUploaderRights: null as boolean | null,
    };
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  mounted: function () {
    checkIfUserHasUploaderRights(this.getKeycloakPromise)
      .then((hasUserUploaderRights) => {
        this.hasUserUploaderRights = hasUserUploaderRights;
      })
      .catch((error) => console.log(error));
  },
});
</script>
