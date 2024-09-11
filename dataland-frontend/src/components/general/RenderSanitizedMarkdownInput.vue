<template>
  <div>
    <MarkdownRender :source="sanitizedMarkdown" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import DOMPurify from 'dompurify';
import MarkdownRender from 'vue-markdown-render';

const props = defineProps<{
  text: string;
}>();

const sanitizedMarkdown = computed(() => {
  try {
    return DOMPurify.sanitize(props.text);
  } catch (error) {
    console.error('Error processing markdown:', error);
    return '';
  }
});
</script>
