<template>
  <span v-html="sanitizedHtml"></span>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';

const props = defineProps<{
  text: string; // Expecting the Markdown text
}>();

const sanitizedHtml = computed(() => {
  try {
    // Convert Markdown to HTML
    const rawHtml = marked(props.text);
    // Sanitize the HTML
    return typeof rawHtml === 'string' ? DOMPurify.sanitize(rawHtml) : '';
  } catch (error) {
    console.error('Error processing markdown:', error);
    return ''; // Return an empty string on error
  }
});
</script>
