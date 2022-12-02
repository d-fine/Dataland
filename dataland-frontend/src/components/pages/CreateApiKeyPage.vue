<template>
  <TheHeader />
  <TheContent class="surface-800">
    <div class="col-12 text-left pb-0">
      <BackButton />
      <h1>Create new API Key</h1>
    </div>

    <div class="col-12 md:col-8 lg:col-6">
      <CreateApiKeyCard />
    </div>

    <div class="col-12 md:col-8 lg:col-6 text-right">
      <Button label="GENERATE API KEY"></Button>
    </div>
  </TheContent>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import Button from "primevue/button";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import BackButton from "@/components/general/BackButton.vue";
import MessageComponent from "@/components/messages/MessageComponent.vue";
import CreateApiKeyCard from "@/components/general/CreateApiKeyCard.vue";

export default defineComponent({
  name: "CreateApiKeyPage",
  components: { TheContent, TheHeader, BackButton, Button, CreateApiKeyCard, MessageComponent },
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
