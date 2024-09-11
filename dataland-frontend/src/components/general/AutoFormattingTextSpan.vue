<template>
  <span>
    <template v-for="(segment, index) in segments" :key="index">
      <template v-if="segment.type === 'text'">{{ segment.text }}</template>
      <template v-else-if="segment.type === 'link' && segment.href.startsWith('https://')">
        <a :href="segment.href" target="_blank" rel="noopener noreferrer">{{ segment.text }}</a>
      </template>
    </template>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { segmentTextIncludingLinks } from '@/utils/LinkExtraction';

const props = defineProps<{
  text: string;
}>();

const segments = computed(() => segmentTextIncludingLinks(props.text));
</script>
