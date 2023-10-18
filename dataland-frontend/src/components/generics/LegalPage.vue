<template>
  <div class="pb-8">
    <div class="grid align-items-center m-0">
      <div class="col-6 text-left">
        <router-link to="/">
          <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pl-3 pt-2" />
        </router-link>
      </div>
    </div>
    <MarginWrapper class="text-left pl-2 pt-2">
      <BackButton />
    </MarginWrapper>
    <div class="w-12 flex justify-content-center">
      <div class="lg:w-6 md:w-10 sm:w-12 text-left">
        <slot />
      </div>
    </div>
  </div>
  <TheFooter />
</template>

<script lang="ts">
import BackButton from "@/components/general/BackButton.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import TheFooter from "@/components/general/TheFooter.vue";

export default {
  name: "LegalPage",
  components: {
    BackButton,
    MarginWrapper,
    TheFooter,
  },
};
</script>
