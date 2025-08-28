<template>
  <div>
    <ContentBox v-for="item in usedBy" :key="item.id" theme="cyan">
      <div><strong>Data Point Type ID:</strong> {{ item.id }}</div>
      <div v-if="item.ref">
        <strong>Reference:</strong>
        <ExternalLink :href="item.ref" />
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
