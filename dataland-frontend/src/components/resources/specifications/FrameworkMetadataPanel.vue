<script setup lang="ts">
import type { FrameworkSpecificationWithParsedSchema } from '@/types/Specifications';
import Card from 'primevue/card';

/**
 * Props for FrameworkMetadataPanel.
 * This is a presentational component that receives all data via props.
 */
const props = defineProps<{
  framework: FrameworkSpecificationWithParsedSchema;
}>();
</script>

<template>
  <Card class="framework-metadata-panel" data-test="framework-metadata">
    <template #title>
      <div class="title-section">
        <h2 class="framework-name">{{ framework.name }}</h2>
        <span class="framework-id">{{ framework.framework.id }}</span>
      </div>
    </template>
    
    <template #content>
      <dl class="metadata-list">
        <div class="metadata-item">
          <dt class="metadata-label">Business Definition</dt>
          <dd class="metadata-value">{{ framework.businessDefinition }}</dd>
        </div>

        <div class="metadata-item">
          <dt class="metadata-label">Framework ID</dt>
          <dd class="metadata-value metadata-value-code">{{ framework.framework.id }}</dd>
        </div>
      </dl>
    </template>
  </Card>
</template>

<style scoped lang="scss">
.framework-metadata-panel {
  .title-section {
    display: flex;
    align-items: baseline;
    gap: 1rem;
    flex-wrap: wrap;

    .framework-name {
      margin: 0;
      font-size: 1.75rem;
      font-weight: 600;
      color: var(--p-text-color);
    }

    .framework-id {
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--p-text-secondary-color);
      padding: 0.25rem 0.75rem;
      background: var(--p-surface-100);
      border-radius: var(--p-border-radius);
    }
  }

  .metadata-list {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    margin: 0;

    .metadata-item {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;

      .metadata-label {
        font-weight: 600;
        color: var(--p-text-color);
        font-size: 0.875rem;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        margin: 0;
      }

      .metadata-value {
        margin: 0;
        color: var(--p-text-color);
        line-height: 1.6;

        &.metadata-value-code {
          font-family: monospace;
          font-size: 0.875rem;
          color: var(--p-text-secondary-color);
        }
      }
    }
  }
}
</style>
