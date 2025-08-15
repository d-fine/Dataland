<template>
  <div>
    <ContentBox v-for="framework in frameworks" :key="framework.id" theme="blue">
      <div><strong>Framework ID:</strong> {{ framework.id }}</div>
      <div v-if="framework.ref">
        <strong>Reference:</strong>
        <ExternalLink :href="framework.ref" />
      </div>
    </ContentBox>
  </div>
</template>

<script setup lang="ts">
import ContentBox from './ContentBox.vue';
import ExternalLink from './ExternalLink.vue';
import type { FrameworkRef } from './types';

interface Props {
  frameworks: FrameworkRef[];
}

defineProps<Props>();
</script>
