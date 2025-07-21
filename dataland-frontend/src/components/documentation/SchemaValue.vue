<template>
  <template v-if="isObject && value !== null">
    <div class="nested-object">
      <template v-for="(nestedValue, nestedKey) in value" :key="nestedKey">
        <div class="schema-property nested" :style="{ marginLeft: `${level * 20}px` }">
          <span class="property-key">{{ nestedKey }}:</span>
          <div class="property-value">
            <template v-if="isNestedObject(nestedValue)">
              <SchemaValue :value="nestedValue" :level="level + 1" @link-click="handleLinkClick" />
            </template>
            <template v-else-if="isDataPointRef(nestedValue)">
              <div class="data-point-ref">
                <a 
                  href="#" 
                  @click.prevent="handleLinkClick(nestedValue.ref)"
                  class="ref-id-link"
                  :title="nestedValue.ref"
                >
                  {{ nestedValue.id }}
                </a>
                <span v-if="nestedValue.aliasExport" class="alias-export">
                  Export alias: {{ nestedValue.aliasExport }}
                </span>
              </div>
            </template>
            <template v-else>
              <span class="simple-value">{{ formatValue(nestedValue) }}</span>
            </template>
          </div>
        </div>
      </template>
    </div>
  </template>
  <template v-else>
    <span class="simple-value">{{ formatValue(value) }}</span>
  </template>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  value: any;
  level: number;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  linkClick: [url: string];
}>();

const isObject = computed(() => typeof props.value === 'object');

const isNestedObject = (val: any): boolean => {
  return typeof val === 'object' && val !== null && !isDataPointRef(val);
};

const isDataPointRef = (val: any): boolean => {
  return typeof val === 'object' && val !== null && val.ref && val.id;
};

const formatValue = (val: any): string => {
  return typeof val === 'string' ? val : JSON.stringify(val);
};

const handleLinkClick = (url: string): void => {
  emit('linkClick', url);
};
</script>

<style lang="scss" scoped>
.schema-property {
  margin-bottom: 0.75rem;
  padding-bottom: 0.25rem;
  border-bottom: 1px solid var(--surface-150);
  
  &.nested {
    margin-bottom: 0.75rem;
    padding-bottom: 0.25rem;
    border-bottom: 1px solid var(--surface-150);
  }
  
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

.data-point-ref {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-left: 1rem;
  padding: 0.75rem;
  background-color: var(--surface-50);
  border-left: 3px solid var(--primary-color);
  border-radius: 0 4px 4px 0;
  
  .ref-id-link {
    font-weight: 600;
    color: var(--blue-500);
    text-decoration: underline;
    cursor: pointer;
    margin-bottom: 0.25rem;
    display: inline-block;
    font-size: 1rem;
    
    &:hover {
      color: var(--blue-700);
      text-decoration: none;
      background-color: var(--blue-50);
      padding: 2px 4px;
      border-radius: 3px;
    }
  }
  
  .alias-export {
    font-style: italic;
    color: var(--text-color-secondary);
    font-size: 0.8rem;
    font-weight: 500;
  }
}

.simple-value {
  color: var(--text-color-secondary);
}

.nested-object {
  margin-top: 0.75rem;
  padding-top: 0.5rem;
}
</style>