<template>
  <TheHeader :landingPage="landingPage" />
<!--  <div class="static-content">-->
    <main role="main">
      <div class="w-12 flex justify-content-center">
        <div class="lg:w-6 md:w-10 sm:w-10 text-justify">
          <slot />
        </div>
      </div>
    </main>
    <TheFooter :sections="landingPage?.sections" />
<!--  </div>-->
</template>

<script lang="ts">
import TheFooter from "@/components/generics/TheNewFooter.vue";
import TheHeader from "@/components/generics/TheNewHeader.vue";
import contentData from "@/assets/content.json";
import type { Content } from "@/types/ContentTypes";
import { defineComponent } from "vue";

const content: Content = contentData;

export default defineComponent({
  name: "LegalPage",
  components: {
    TheHeader,
    TheFooter,
  },
  data() {
    return {
      landingPage: content.pages.find((page) => page.url === "/"),
    };
  },
});
</script>

<style lang="scss" scoped>
.static-content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 100vh;
}

main {
  margin-top: 132px;
  margin-bottom: 52px;
  @media only screen and (max-width: $small) {
    margin-top: 52px;
  }
}
</style>
