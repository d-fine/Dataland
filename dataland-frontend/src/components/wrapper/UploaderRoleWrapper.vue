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
import { defineComponent } from "vue";
import { checkIfUserHasUploaderRights, KeycloakComponentSetup } from "@/utils/KeycloakUtils";
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
    return KeycloakComponentSetup;
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
