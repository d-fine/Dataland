<template>
  <ContentBox theme="purple">
    <div v-if="example.value !== undefined" style="margin-bottom: var(--spacing-xs)">
      <strong>Value:</strong>
      <code>
        {{ formatValue(example.value) }}
      </code>
    </div>

    <div v-if="example.quality" style="margin-bottom: var(--spacing-xs)">
      <strong>Quality:</strong> {{ example.quality }}
    </div>

    <div v-if="example.comment" style="margin-bottom: var(--spacing-xs)">
      <strong>Comment:</strong> {{ example.comment }}
    </div>

    <div v-if="example.dataSource">
      <strong>Data Source:</strong>
      <div style="margin-left: var(--spacing-md); margin-top: var(--spacing-xs)">
        <div v-if="example.dataSource.fileName"><strong>File:</strong> {{ example.dataSource.fileName }}</div>
        <div v-if="example.dataSource.page"><strong>Page:</strong> {{ example.dataSource.page }}</div>
        <div v-if="example.dataSource.tagName"><strong>Tag:</strong> {{ example.dataSource.tagName }}</div>
        <div v-if="example.dataSource.publicationDate">
          <strong>Publication Date:</strong> {{ example.dataSource.publicationDate }}
        </div>
        <div v-if="example.dataSource.fileReference" style="word-break: break-all">
          <strong>File Reference:</strong>
          <code style="font-size: var(--font-size-xs)">
            {{ example.dataSource.fileReference }}
          </code>
        </div>
      </div>
    </div>
  </ContentBox>
</template>

<script setup lang="ts">
import ContentBox from './ContentBox.vue';
import type { Example } from './types';

interface Props {
  example: Example;
}

defineProps<Props>();

const formatValue = (value: any): string => {
  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2);
  }
  return String(value);
};
</script>
