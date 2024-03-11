<template>
  <AuthenticationWrapper>
    <TheHeader />
    <div class="sheet">
      <div class="headline">
        <BackButton />
      </div>
      <h1 class="text-left">Data Request</h1>
    </div>
    <div class="py-4 justify-content-center">
      <div class="col-6">Test blabla {{ requestId }}</div>
    </div>
    <TheFooter :is-light-version="true" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import BackButton from "@/components/general/BackButton.vue";
import TheFooter from "@/components/generics/TheFooter.vue";
export default defineComponent({
  name: "ViewDataRequest",
  components: { BackButton, AuthenticationWrapper, TheHeader, TheFooter },
  props: {
    requestId: {
      type: String,
      required: false, //todo
      default: "12345",
    },
  },
});
</script>
