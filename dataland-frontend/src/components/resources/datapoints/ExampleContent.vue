<template>
  <ContentBox theme="purple">
    <div v-if="getExampleValue(example) !== undefined">
      <strong>Value:</strong>
      <code>
        {{ formatValue(getExampleValue(example)) }}
      </code>
    </div>

    <div v-if="getExampleObject(example).quality">
      <strong>Quality:</strong> {{ getExampleObject(example).quality }}
    </div>

    <div v-if="getExampleObject(example).comment">
      <strong>Comment:</strong> {{ getExampleObject(example).comment }}
    </div>

    <div v-if="getExampleObject(example).dataSource">
      <strong>Data Source:</strong>
      <div>
        <div v-if="getExampleObject(example).dataSource.fileName">
          <strong>File:</strong> {{ getExampleObject(example).dataSource.fileName }}
        </div>
        <div v-if="getExampleObject(example).dataSource.page">
          <strong>Page:</strong> {{ getExampleObject(example).dataSource.page }}
        </div>
        <div v-if="getExampleObject(example).dataSource.tagName">
          <strong>Tag:</strong> {{ getExampleObject(example).dataSource.tagName }}
        </div>
        <div v-if="getExampleObject(example).dataSource.publicationDate">
          <strong>Publication Date:</strong> {{ getExampleObject(example).dataSource.publicationDate }}
        </div>
        <div v-if="getExampleObject(example).dataSource.fileReference">
          <strong>File Reference:</strong>
          <code>
            {{ getExampleObject(example).dataSource.fileReference }}
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
  example: Example | string | number | boolean;
}

defineProps<Props>();

const formatValue = (value: any): string => {
  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2);
  }
  return String(value);
};

// Handle both object and primitive example values
const getExampleValue = (example: any) => {
  if (typeof example === 'object' && example !== null) {
    return example.value;
  }
  // If example is a primitive value (string, number, etc.)
  return example;
};

const getExampleObject = (example: any) => {
  if (typeof example === 'object' && example !== null) {
    return example;
  }
  // If example is a primitive, create a minimal object
  return { value: example };
};
</script>
