<template>
  <div id="jumpLinks" ref="jumpLinks">
    <h4 id="topicTitles" class="title pt-3">On this page</h4>
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
  }),
});
</script>
