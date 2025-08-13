<template>
  <div>
    <ContentBox
      v-for="(framework, index) in frameworks"
      :key="framework.id"
      theme="blue"
      :style="{ marginTop: index > 0 ? 'var(--spacing-sm)' : '0' }"
    >
      <div><strong>Framework ID:</strong> {{ framework.id }}</div>
      <div v-if="framework.ref" style="margin-top: var(--spacing-xxxs)">
        <strong>Reference:</strong>
        <ExternalLink :href="framework.ref">{{ framework.ref }}</ExternalLink>
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
