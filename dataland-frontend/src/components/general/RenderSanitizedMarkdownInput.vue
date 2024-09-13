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

/**
 * Method to replace raw HTTP URLs with Markdown-type URLs: [URL presentation](URL)
 * @param text Input text
 * @returns a converted input text (text remains unchanged if no matching HTTP URLS are found)
 */
function convertRawUrlsToMarkdown(text: string): string {
  const urlPattern = /https:\/\/\S+/; // Regex pattern to match URLs
  return text.replace(urlPattern, (url) => `[${url}](${url})`);
}

const sanitizedMarkdown = computed(() => {
  try {
    const markdownWithLinks = convertRawUrlsToMarkdown(props.text);
    return DOMPurify.sanitize(markdownWithLinks);
  } catch (error) {
    console.error('Error processing markdown:', error);
    return '';
  }
});
</script>
