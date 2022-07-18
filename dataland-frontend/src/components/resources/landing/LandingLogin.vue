<template>
  <div class="surface-ground pb-8">
    <div class="m-0">
      <div class="grid align-items-center m-0">
        <div class="col-1 text-left p-0">
          <img src="@/assets/images/logos/preview_banner.svg" alt="Preview Banner" />
        </div>
        <div class="col-5 text-left">
          <router-link to="/">
            <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pl-3 pt-2" />
          </router-link>
        </div>
        <div class="col-2 col-offset-4">
          <UserAuthenticationButtons />
        </div>
      </div>
      <div class="grid align-items-center m-0">
        <div class="col-5 col-offset-1">
          <h1 class="text-justify text-6xl font-normal">
            COME TOGETHER TO CREATE A DATASET THAT NOBODY CAN CREATE ALONE WHILE SHARING THE COSTS.
          </h1>
        </div>
        <div class="col-4 col-offset-1">
          <router-link to="/searchtaxonomy">
            <img alt="Dataland logo" src="@/assets/images/logos/bg_graphic_vision.svg" class="mx-auto" />
          </router-link>
        </div>
      </div>
      <div class="grid text-left m-0">
        <div class="col-10 col-offset-1 pb-0">
          <p class="uppercase mb-0">Built by</p>
        </div>
        <div class="col-1 col-offset-1 pt-0">
          <img src="@/assets/images/elements/orange_short_line.svg" alt="short orange line" />
        </div>
      </div>
      <div class="grid text-left m-0">
        <div class="col-offset-1 col-10">
          <div class="grid align-items-baseline">
            <div class="col-1">
              <img src="@/assets/images/logos/pwc.svg" alt="pwc" class="pr-5" />
            </div>
            <div class="col-1">
              <img src="@/assets/images/logos/dfine.svg" alt="d-fine GmbH" class="d-small-logo pr-5" />
            </div>
          </div>
        </div>
      </div>
      <div class="grid m-0">
        <div class="col-5 col-offset-3">
          <Card class="d-card">
            <template #title>
              <h2 class="text-gray-100 text-left">Join Dataland to access our data</h2>
            </template>
            <template #content>
              <p class="text-gray-800 text-left">
                Register free to access Eu Taxonomy data from more than <strong>300</strong> Germany public companies.
              </p>
              <div class="grid">
                <div class="col-10 col-offset-1 p-fluid pl-0">
                  <PrimeButton
                    class="uppercase p-button p-button pl-2 pr-1 pb-1 pt-1 justify-content-center h-2rem w-full"
                    name="join_dataland_button_center"
                    @click="register"
                  >
                    <span class="d-letters d-button"> Join Dataland </span>
                    <i class="material-icons pl-1" aria-hidden="true">chevron_right</i>
                  </PrimeButton>
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Card from "primevue/card";
import PrimeButton from "primevue/button";
import UserAuthenticationButtons from "@/components/general/UserAuthenticationButtons";

export default {
  name: "LandingLogin",
  components: { UserAuthenticationButtons, Card, PrimeButton },
  inject: ["authenticated", "getKeycloakInitPromise"],
  methods: {
    register() {
      this.getKeycloakInitPromise()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            return keycloak.register();
          }
        })
        .catch((error) => console.log("error: " + error));
    },
  },
};
</script>
