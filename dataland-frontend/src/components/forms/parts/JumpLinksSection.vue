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
import { defineComponent, type PropType } from 'vue';
import { smoothScroll } from '@/utils/SmoothScroll';
import { type ClickableLink } from '@/types/CustomPropTypes';

export default defineComponent({
  name: 'JumpLinksSection',
  props: {
    onThisPageLinks: {
      type: Array as PropType<ClickableLink[]>,
      required: true,
    },
  },
  data: () => ({
    smoothScroll,
  }),
});
</script>
