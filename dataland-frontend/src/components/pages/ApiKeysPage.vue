<template>
  <TheHeader />
  <TheContent class="surface-800">
    <MarginWrapper class="text-left mt-2">
      <BackButton />
      <div class="col-12 text-left pb-0">
        <h1>API</h1>
      </div>
    </MarginWrapper>

    <MiddleCenterDiv  v-if="!thereIsApiKey" class="col-12">
      <div>
        <img src="@/assets/images/elements/bulb_icon.svg" />
        <p class="font-medium text-xl text-color-third">You have no API Key!</p>
        <Button label="CREATE NEW API KEY" icon="pi pi-plus"></Button>
      </div>
    </MiddleCenterDiv>

    <div v-if="thereIsApiKey && !waitingForData">

      <div class="col-12 md:col-8 lg:col-6">
        <ApiKeyCard />
      </div>
    </div>
  </TheContent>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import Button from "primevue/button";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import ApiKeyCard from "@/components/general/ApiKeyCard.vue";

export default defineComponent({
  name: "ApiKeysPage",
  components: { TheContent, TheHeader, MiddleCenterDiv, BackButton, MarginWrapper, Button, ApiKeyCard, },
  setup() {
    return {};
  },
  data() {
    return {
      thereIsApiKey: true,
      waitingForData: false,
    };
  },
  props: {},
  mounted() {},
  watch: {},
  methods: {},
});
</script>

<style scoped></style>
