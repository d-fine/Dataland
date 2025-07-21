<template>
  <div class="surface-card shadow-1 p-4 border-round">
    <h3 class="text-900 font-medium text-lg mb-3">{{ title }}</h3>
    
    <div v-if="parsedSchema" class="schema-container">
      <div class="schema-content">
        <div class="schema-object">
          <template v-for="(value, key) in parsedSchema" :key="key">
            <div class="schema-property">
              <span class="property-key">{{ key }}:</span>
              <div class="property-value">
                <SchemaValue :value="value" :level="1" @link-click="handleLinkClick" />
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
    
    <div v-else-if="rawData" class="example-container">
      <p v-if="showRawLabel" class="text-600 mb-2">{{ rawLabel }}:</p>
      <pre class="example-json">{{ typeof rawData === 'string' ? rawData : JSON.stringify(rawData, null, 2) }}</pre>
    </div>
    
    <div v-else class="text-center p-4">
      <p class="text-600">{{ emptyMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import SchemaValue from './SchemaValue.vue';

interface Props {
  title: string;
  data?: any;
  rawLabel?: string;
  showRawLabel?: boolean;
  emptyMessage?: string;
}

const props = withDefaults(defineProps<Props>(), {
  rawLabel: 'Raw Schema (JSON string)',
  showRawLabel: true,
  emptyMessage: 'No schema data available',
});

const emit = defineEmits<{
  linkClick: [url: string];
}>();

const parsedSchema = computed(() => {
  if (!props.data) return null;
  
  if (typeof props.data === 'string') {
    try {
      return JSON.parse(props.data);
    } catch {
      return null;
    }
  }
  
  if (typeof props.data === 'object') {
    return props.data;
  }
  
  return null;
});

const rawData = computed(() => {
  if (parsedSchema.value) return null;
  return props.data;
});

const handleLinkClick = (url: string): void => {
  emit('linkClick', url);
};
</script>

<style lang="scss" scoped>
.example-container {
  background-color: var(--surface-100);
  border: 1px solid var(--surface-300);
  border-radius: 6px;
  padding: 1rem;
  overflow-x: auto;

  .example-json {
    font-family: 'Courier New', 'Monaco', 'Menlo', monospace;
    font-size: 0.875rem;
    margin: 0;
    white-space: pre;
    color: var(--text-color);
    line-height: 1.4;
  }
}

.schema-container {
  background-color: var(--surface-100);
  border: 1px solid var(--surface-300);
  border-radius: 6px;
  padding: 1rem;
  overflow-x: auto;
  
  .schema-content {
    font-family: 'Courier New', 'Monaco', 'Menlo', monospace;
    font-size: 0.875rem;
    line-height: 1.4;
    color: var(--text-color);
  }
}

.schema-property {
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--surface-200);
  
  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
  }
  
  .property-key {
    font-weight: 600;
    color: var(--primary-color);
    margin-right: 0.75rem;
    display: block;
    margin-bottom: 0.25rem;
  }
  
  .property-value {
    display: block;
    margin-left: 0.5rem;
  }
}

// Responsive adjustments
@media (max-width: 768px) {
  .example-container {
    padding: 0.75rem;
    
    .example-json {
      font-size: 0.8rem;
    }
  }
}
</style>