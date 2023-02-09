<template>
  <div class="surface-ground pb-8">
    <div class="align-items-center">
      <div data-test="landing-page-top-logo" class="mobile-logo">
        <router-link to="/">
          <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pt-2" />
        </router-link>
      </div>
    </div>

    <h2 class="font-light d-letters text-4xl m-6 text-left">
      We are working to make the website display properly on mobile. <br />
      For now, however, we invite you to the desktop version.
    </h2>

    <img alt="Dataland logo" src="@/assets/images/logos/bg_graphic_vision.svg" class="mx-auto max-w-full" />

    <BuildersAndSponsors />
  </div>
</template>

<script lang="ts">
import BuildersAndSponsors from "@/components/resources/landing/BuildersAndSponsors.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "LandingLoginMobile",
  components: { BuildersAndSponsors },
});
</script>
<style scoped lang="scss">
.d-text-register {
  color: #5a4f36;
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
