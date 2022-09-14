<template>
  <div class="pb-8">
    <div class="m-0">
      <div class="grid align-items-center m-0">
        <div class="col-6 text-left">
          <router-link to="/">
            <img
              src="@/assets/images/logos/logo_dataland_long.svg"
              alt="Dataland Logo"
              class="pl-3 pt-2"
            />
          </router-link>
        </div>
      </div>
      <MarginWrapper class="text-left pl-2">
        <BackButton />
      </MarginWrapper>
      <MarginWrapper class="text-left pl-2">
        <div class="col-6 text-justify">
          <h2>Data Privacy</h2>
          <p>Soon you will find our data privacy information here.</p>
        </div>
      </MarginWrapper>
    </div>
  </div>
</template>

<script>
import BackButton from "@/components/general/BackButton.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";

export default {
  name: "DataPrivacy",
  components: {
    BackButton,
    MarginWrapper,
  },
};
</script>
