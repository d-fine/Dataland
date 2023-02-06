<template>
  <div class="surface-ground pb-8">
    <div class="m-0">
      <div data-test="landing-page-top-info-message" v-if="!isMobile" class="grid align-items-center m-0 third-section">
        <div class="col-9 col-offset-1">
          <p class="text-white text-left text-xl pl-3 pr-3">
            This preview page is intended to give a first impression of Dataland's goals and has no further
            functionality. Dataland is currently being founded and has therefore not yet started operations.
          </p>
        </div>
      </div>
      <div class="grid align-items-center m-0">
        <div data-test="landing-page-top-logo" :class="[!isMobile ? ['col-5 text-left col-offset-1'] : 'mobile-logo']">
          <router-link to="/">
            <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pt-2" />
          </router-link>
        </div>
        <div data-test="landing-page-login-button" v-if="!isMobile" class="col-2 col-offset-3">
          <UserAuthenticationButtons />
        </div>
      </div>

      <h2
        data-test="landing-page-mobile-description"
        v-if="isMobile"
        class="font-light d-letters text-4xl m-6 text-left"
      >
        We are working to make the website display properly on mobile. <br />
        For now, however, we invite you to the desktop version.
      </h2>

      <div data-test="landing-page-graphic-vision" class="grid align-items-center m-0" :class="{ isMobile: isMobile }">
        <div v-if="!isMobile" class="col-5 col-offset-1">
          <h1 class="text-left text-6xl font-normal">THE ALTERNATIVE TO DATA MONOPOLIES</h1>
        </div>
        <div data-test="landing-page-graphic-vision-img" :class="[!isMobile ? ['col-4', 'col-offset-1'] : '']">
          <img alt="Dataland logo" src="@/assets/images/logos/bg_graphic_vision.svg" class="mx-auto" />
        </div>
      </div>
      <div data-test="landing-page-create-account" v-if="!isMobile" class="grid m-0">
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

      <div class="grid center-divs-row-wrapper">
        <div class="center-divs-row">
          <div>
            <p class="uppercase mb-0">Built by</p>
            <img src="@/assets/images/elements/orange_short_line.svg" alt="short orange line" />
            <div class="imgs-in-line">
              <img src="@/assets/images/logos/pwc.svg" alt="pwc" />
              <img src="@/assets/images/logos/dfine.svg" alt="d-fine GmbH" class="d-small-logo" />
            </div>
          </div>
          <div>
            <p class="uppercase mb-0">Supported by</p>
            <img src="@/assets/images/elements/orange_short_line.svg" alt="short orange line" />
            <div class="imgs-in-line">
              <img src="@/assets/images/logos/t_systems_logo.svg" alt="T-Systems" class="d-small-logo" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import Card from "primevue/card";
import UserAuthenticationButtons from "@/components/general/UserAuthenticationButtons.vue";
import JoinDatalandButton from "@/components/general/JoinDatalandButton.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "LandingLogin",
  components: { JoinDatalandButton, UserAuthenticationButtons, Card },
  props: {
    isMobile: {
      type: Boolean,
    },
  },
});
</script>
<style scoped lang="scss">
.d-text-register {
  color: #5a4f36;
}
.isMobile {
  display: flex;
  flex-direction: column-reverse;
  img {
    max-width: 100%;
  }
}
.mobile-logo {
  text-align: center;
  margin: 2rem;
  width: 100%;
}
.center-divs-row-wrapper {
  justify-content: center;
  .center-divs-row {
    display: flex;
    text-align: left;
    width: 80%;
    justify-content: space-around;
    max-width: 400px;
    margin: 1rem;
  }
}
.imgs-in-line {
  display: flex;
  justify-content: space-between;
  width: 100%;
  height: 60px;
  align-items: flex-end;
  img {
    margin: 0.5rem 2vw 0.5rem 0;
  }
}
</style>
