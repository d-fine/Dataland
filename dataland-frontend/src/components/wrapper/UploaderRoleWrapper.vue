<template>
  <div v-if="hasUserUploaderRights">
    <slot></slot>
  </div>
  <TheContent v-else class="paper-section flex">
    <MiddleCenterDiv class="col-12">
      <div class="col-6 md:col-8 lg:col-12">
        <h2>You can not visit this site because you have no uploader status.</h2>
      </div>
    </MiddleCenterDiv>
  </TheContent>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { checkIfUserHasUploaderRights } from "@/utils/KeycloakUtils";
import TheContent from "@/components/generics/TheContent.vue";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";

export default defineComponent({
  name: "UploaderRoleWrapper",
  components: { TheContent, MiddleCenterDiv },
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
