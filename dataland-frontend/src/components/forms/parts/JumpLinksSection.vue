<template>
  <div id="jumpLinks" ref="jumpLinks" class="col-3 p-3 text-left jumpLinks">
    <h4 id="topicTitles" class="title">On this page</h4>
    <ul>
      <li v-for="(element, index) in onThisPageLinks" :key="index">
        <a @click="smoothScroll(`#${element.value}`)">{{ element.label }}</a>
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { smoothScroll } from "@/utils/smoothScroll";

export default defineComponent({
  name: "JumpLinksSection",
  props: {
    onThisPageLinks: {
      type: Array,
      required: true,
    },
  },
  data: () => ({
    smoothScroll,
    scrollListener: (): null => null,
  }),
  mounted() {
    const jumpLinkselement = this.$refs.jumpLinks as HTMLElement;

    const elementPosition = jumpLinkselement.getBoundingClientRect().top;
    this.scrollListener = (): null => {
      if (window.scrollY > elementPosition) {
        jumpLinkselement.style.position = "fixed";
        jumpLinkselement.style.top = "60px";
      } else {
        jumpLinkselement.style.position = "relative";
        jumpLinkselement.style.top = "0";
      }
      return null;
    };
    window.addEventListener("scroll", this.scrollListener);
  },
  unmounted() {
    window.removeEventListener("scroll", this.scrollListener);
  },
});
</script>
