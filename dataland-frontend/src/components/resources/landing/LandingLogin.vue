<template>
  <div class="surface-ground pb-8">
    <div class="m-0">
      <div data-test="landing-page-top-info-message" class="grid align-items-center m-0 third-section">
        <div class="col-9 col-offset-1">
          <p class="text-white text-left text-xl pl-3 pr-3">
            This preview page is intended to give a first impression of Dataland's goals and has no further
            functionality. Dataland is currently being founded and has therefore not yet started operations.
          </p>
        </div>
      </div>
      <div class="grid align-items-center m-0">
        <div data-test="landing-page-top-logo" class="col-5 text-left col-offset-1">
          <router-link to="/">
            <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland banner logo" class="pt-2" />
          </router-link>
        </div>
        <div data-test="landing-page-login-button" class="col-2 col-offset-3">
          <UserAuthenticationButtons />
        </div>
      </div>

      <div data-test="landing-page-graphic-vision" class="grid align-items-center m-0">
        <div class="col-5 col-offset-1">
          <h1 class="text-left text-6xl font-normal" data-test="banner message">THE ALTERNATIVE TO DATA MONOPOLIES</h1>
        </div>
        <div data-test="landing-page-graphic-vision-img" class="col-4 col-offset-1">
          <img alt="Dataland image logo" src="@/assets/images/logos/bg_graphic_vision.svg" class="mx-auto" />
        </div>
      </div>
      <div data-test="landing-page-create-account" class="grid m-0">
        <div class="col-4 col-offset-4">
          <Card class="d-card">
            <template #title>
              <p class="text-left text-xxl pl-3 pr-3">Create a preview account to try Dataland</p>
            </template>
            <template #content>
              <p class="d-text-register text-left text-xl pl-3 pr-3">
                Create a preview account to <strong>try Dataland</strong> and access data for
                <strong>EU Taxonomy</strong> and other frameworks.
              </p>
              <div class="grid">
                <div class="col-12 p-fluid pl-3 pr-3">
                  <JoinDatalandButton />
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>

      <BuildersAndSponsors />
    </div>
  </div>
</template>

<script lang="ts">
import Card from "primevue/card";
import UserAuthenticationButtons from "@/components/general/UserAuthenticationButtons.vue";
import JoinDatalandButton from "@/components/general/JoinDatalandButton.vue";
import BuildersAndSponsors from "@/components/resources/landing/BuildersAndSponsors.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "LandingLogin",
  components: { JoinDatalandButton, UserAuthenticationButtons, Card, BuildersAndSponsors },
});
</script>
<style scoped lang="scss">
.d-text-register {
  color: #5a4f36;
}
</style>
