<template>
  <div>
    <vue-markdown :source="sanitizedMarkdown" :options="displayRenderedMarkdownOptions" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import DOMPurify from 'dompurify';
import VueMarkdown from 'vue-markdown-render';
import MarkdownIt from 'markdown-it';

const props = defineProps<{
  text: string;
}>();

const renderMarkdownInputOptions = {
  html: false,
  linkify: true,
  breaks: true,
};

const displayRenderedMarkdownOptions = {
  html: true,
  linkify: true,
  breaks: true,
};

const md = new MarkdownIt(renderMarkdownInputOptions);

const sanitizedMarkdown = computed(() => {
  const renderedMarkdown = md.render(props.text);
  return DOMPurify.sanitize(renderedMarkdown);
});
</script>
