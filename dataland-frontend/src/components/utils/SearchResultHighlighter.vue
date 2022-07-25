<template>
  <template v-for="(element, index) in splitString" :key="index">
    <span :class="element.highlight ? 'font-semibold' : 'font-normal'">{{ element.text }}</span>
  </template>
</template>

<style scoped>
span {
  white-space: pre;
}
</style>

<script>
import { splitStringBySearchMatch } from "@/utils/StringHighlighter";

export default {
  name: "SearchResultHighlighter",
  props: {
    text: {
      type: String,
      default: "Hithere",
    },
    searchString: {
      type: String,
      default: "",
    },
  },
  computed: {
    splitString() {
      return splitStringBySearchMatch(this.text, this.searchString);
    },
  },
};
</script>
