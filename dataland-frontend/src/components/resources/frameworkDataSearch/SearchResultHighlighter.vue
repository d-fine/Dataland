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

<script lang="ts">
import { splitStringBySearchMatch, type StringSplitSearchMatch } from '@/utils/StringHighlighter';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'SearchResultHighlighter',
  props: {
    text: {
      type: String,
      default: '',
    },
    searchString: {
      type: String,
      default: '',
    },
  },
  computed: {
    splitString(): Array<StringSplitSearchMatch> {
      return splitStringBySearchMatch(this.text, this.searchString);
    },
  },
});
</script>
