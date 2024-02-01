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
import TheContent from "@/components/generics/TheContent.vue";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { type AxiosError } from "axios/index";

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
    console.log("Priantus");
    this.isUserDataOwnerForCompany(this.companyId, this.getKeycloakPromise())
      .then((isUserDataOwner) => {
        this.isUserDataOwner = isUserDataOwner;
      })
      .catch((error) => console.log(error));
  },
  methods: {
    async isUserDataOwnerForCompany(
      companyId: string,
      keycloakPromiseGetter: () => Promise<Keycloak>,
    ): Promise<boolean> {
      // const resolvedKeycloakPromise = await waitForAndReturnResolvedKeycloakPromise(keycloakPromiseGetter);
      //  const userId = resolvedKeycloakPromise.idToken
      const userId = (await keycloakPromiseGetter()).idTokenParsed.sub;
      console.log("Here")
      console.log(userId);

      try {
        await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).backendClients.companyDataController.isUserDataOwnerForCompany(companyId, assertDefined(userId));
        console.log("Printus");
        return (this.isUserDataOwner = true);
      } catch (error: AxiosError) {
          console.log("error")
        if ((error as AxiosError).response.status == 404) {
          return (this.isUserDataOwner = false);
        }
        throw error;
      }
    },
  },
});
</script>
