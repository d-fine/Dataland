<template>
  <div v-if="!authenticated" class="grid align-items-center m-0">
    <div class="col-6">
      <h1 class="text-justify text-base font-normal">
        Checking Log-In status.
        <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </h1>
    </div>
  </div>
  <slot v-if="authenticated"></slot>
</template>

<script>
export default {
  name: "AuthenticationWrapper",
  inject: ["authenticated", "getKeycloakInitPromise"],

  mounted: function () {
    if (!this.authenticated) {
      this.getKeycloakInitPromise()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            return keycloak.login();
          }
        })
        .catch((error) => console.log("error: " + error));
    }
  },
};
</script>
