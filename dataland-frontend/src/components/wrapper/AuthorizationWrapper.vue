<template>
  <div v-if="!hasUserRequiredRole && waitingForDataOwnershipData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Checking for data ownership...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="hasUserRequiredRole || isUserDataOwner">
    <slot></slot>
  </div>

  <TheContent v-if="!waitingForDataOwnershipData" class="paper-section flex">
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
      waitingForDataOwnershipData: true,
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
      .then(() => {
        if (!this.hasUserRequiredRole) {
          isUserDataOwnerForCompany(this.companyId, this.getKeycloakPromise)
            .then((isUserDataOwner) => {
              this.isUserDataOwner = isUserDataOwner;
              this.waitingForDataOwnershipData = false;
            })
            .catch((error) => console.log(error));
        }
      })
      .catch((error) => console.log(error));
  },
});
</script>
