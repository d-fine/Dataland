<template>
  <div>
    <ContentBox
      v-for="(item, index) in usedBy"
      :key="item.id"
      theme="cyan"
      :style="{ marginTop: index > 0 ? 'var(--spacing-sm)' : '0' }"
    >
      <div><strong>Data Point Type ID:</strong> {{ item.id }}</div>
      <div v-if="item.ref" style="margin-top: var(--spacing-xxxs)">
        <strong>Reference:</strong>
        <ExternalLink :href="item.ref">{{ item.ref }}</ExternalLink>
      </div>
    </ContentBox>
  </div>
</template>

<script setup lang="ts">
import ContentBox from './ContentBox.vue';
import ExternalLink from './ExternalLink.vue';
import type { UsedByItem } from './types';

interface Props {
  usedBy: UsedByItem[];
}

defineProps<Props>();
</script>
