<script setup lang="ts">
import { ref, onMounted } from 'vue';
import type { ParsedSchemaNode, ParsedSection, ParsedDataPoint } from '@/types/Specifications';
import { humanizeStringOrNumber, humanizeDataPointBaseType } from '@/utils/StringFormatter';
import PrimeButton from 'primevue/button';

/**
 * Props for SpecificationSchemaTree.
 * This is a presentational component that receives the parsed schema via props.
 */
const props = defineProps<{
  schema: ParsedSchemaNode[];
}>();

/**
 * Emits for SpecificationSchemaTree.
 */
const emit = defineEmits<{
  'view-details': [dataPointTypeId: string];
}>();

// Track expanded sections using a Set of section IDs
const expandedSections = ref<Set<string>>(new Set());

/**
 * Auto-expand top-level sections on mount.
 */
onMounted(() => {
  // Expand all top-level sections
  props.schema.forEach((node) => {
    if (node.type === 'section') {
      expandedSections.value.add(node.id);
    }
  });
});

/**
 * Toggle section expansion state.
 */
function toggleSection(sectionId: string): void {
  if (expandedSections.value.has(sectionId)) {
    expandedSections.value.delete(sectionId);
  } else {
    expandedSections.value.add(sectionId);
  }
}

/**
 * Check if a section is expanded.
 */
function isSectionExpanded(sectionId: string): boolean {
  return expandedSections.value.has(sectionId);
}

/**
 * Get the human-readable display name for a data point.
 * Transforms technical names like "COMPANY_NAME" to "Company Name".
 */
function getDataPointDisplayName(dataPoint: ParsedDataPoint): string {
  return humanizeStringOrNumber(dataPoint.aliasExport || dataPoint.key);
}

/**
 * Extract and display both human-readable and technical data type information.
 * @param dataPointTypeId - The full data point type ID (e.g., "plainDateSfdrDataDate")
 * @returns Object with readable label and technical ID
 */
function getDataTypeDisplay(dataPointTypeId: string): { readable: string; technical: string } {
  // Extract base type prefix using regex (e.g., "plainDate" from "plainDateSfdrDataDate")
  const baseTypeMatch = dataPointTypeId.match(/^(plain[A-Z][a-z]+|extended[A-Z][a-z]+)/);
  const baseType = baseTypeMatch ? baseTypeMatch[1] : dataPointTypeId;
  
  return {
    readable: humanizeDataPointBaseType(baseType),
    technical: dataPointTypeId,
  };
}

/**
 * Get the appropriate PrimeIcon for a data point based on its type.
 * @param dataPointTypeId - The full data point type ID
 * @returns PrimeIcon class name
 */
function getDataPointIcon(dataPointTypeId: string): string {
  // Extract base type prefix
  const baseTypeMatch = dataPointTypeId.match(/^(plain[A-Z][a-z]+|extended[A-Z][a-z]+)/);
  const baseType = baseTypeMatch ? baseTypeMatch[1] : '';
  
  // Map base types to PrimeIcons
  if (baseType.includes('Date')) return 'pi pi-calendar';
  if (baseType.includes('String')) return 'pi pi-align-left';
  if (baseType.includes('Integer') || baseType.includes('Decimal')) return 'pi pi-hashtag';
  if (baseType.includes('Boolean')) return 'pi pi-check-circle';
  if (baseType.includes('Array')) return 'pi pi-list';
  if (baseType.includes('Enum')) return 'pi pi-th-large';
  
  return 'pi pi-circle';
}

/**
 * Handle keyboard navigation for section headers.
 */
function handleSectionKeydown(event: KeyboardEvent, sectionId: string): void {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    toggleSection(sectionId);
  }
}

/**
 * Emit view-details event for a data point.
 */
function handleViewDetails(dataPointTypeId: string): void {
  emit('view-details', dataPointTypeId);
}
</script>

<template>
  <div class="specification-schema-tree">
    <template v-for="node in schema" :key="node.id">
      <!-- Section (expandable container) -->
      <div
        v-if="node.type === 'section'"
        class="schema-section"
        :data-section-id="node.id"
      >
        <div
          class="section-header"
          :class="{ 'section-header-expanded': isSectionExpanded(node.id) }"
          role="button"
          tabindex="0"
          :aria-expanded="isSectionExpanded(node.id)"
          :data-test="'section-header'"
          @click="toggleSection(node.id)"
          @keydown="handleSectionKeydown($event, node.id)"
        >
          <i
            v-if="isSectionExpanded(node.id)"
            class="pi pi-chevron-down section-icon"
            aria-hidden="true"
          />
          <i
            v-else
            class="pi pi-chevron-left section-icon"
            aria-hidden="true"
          />
          <span class="section-label">{{ node.label }}</span>
        </div>

        <!-- Recursively render children if expanded -->
        <div v-if="isSectionExpanded(node.id)" class="section-content">
          <SpecificationSchemaTree
            :schema="node.children"
            @view-details="handleViewDetails"
          />
        </div>
      </div>

      <!-- Data Point (leaf node) -->
      <div
        v-else-if="node.type === 'dataPoint'"
        class="data-point"
        :data-datapoint-id="node.id"
      >
        <div class="data-point-header">
          <i :class="getDataPointIcon(node.dataPointTypeId)" class="data-point-icon" aria-hidden="true"></i>
          <span class="data-point-name" :data-test="'datapoint-name'">
            {{ getDataPointDisplayName(node) }}
          </span>
          <PrimeButton
            label="View Details"
            icon="pi pi-info-circle"
            size="small"
            text
            :data-test="'view-details-button'"
            @click="handleViewDetails(node.dataPointTypeId)"
          />
        </div>

        <!-- Data Point Type (human-readable + technical) -->
        <div class="data-point-type">
          <span class="type-label">Type:</span>
          <span class="type-readable">{{ getDataTypeDisplay(node.dataPointTypeId).readable }}</span>
          <span class="type-technical">({{ node.dataPointTypeId }})</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.specification-schema-tree {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;

  .schema-section {
    border: 1px solid var(--p-surface-border);
    border-radius: var(--p-border-radius);
    background: var(--p-surface-card);
    overflow: hidden;

    .section-header {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem;
      cursor: pointer;
      background: var(--p-surface-0);
      transition: background-color 0.2s;

      &:hover {
        background: var(--p-surface-hover);
      }

      &:focus {
        outline: 2px solid var(--p-primary-color);
        outline-offset: -2px;
      }

      &.section-header-expanded {
        border-bottom: 1px solid var(--p-surface-border);
        border-left: 3px solid var(--p-primary-color);
      }

      .section-icon {
        font-size: 1rem;
        color: var(--p-text-secondary-color);
      }

      .section-label {
        font-weight: 600;
        font-size: 1.125rem;
        color: var(--p-text-color);
      }
    }

    .section-content {
      padding: 1rem;
    }
  }

  .data-point {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    padding: 1rem;
    border: 1px solid var(--p-surface-border);
    border-radius: var(--p-border-radius);
    background: var(--p-surface-card);

    .data-point-header {
      display: flex;
      align-items: center;
      gap: 0.75rem;

      .data-point-icon {
        font-size: 1.125rem;
        color: var(--p-primary-color);
        flex-shrink: 0;
      }

      .data-point-name {
        font-weight: 600;
        font-size: 1rem;
        color: var(--p-text-color);
        flex-grow: 1;
      }
    }

    .data-point-type {
      display: flex;
      align-items: baseline;
      gap: 0.5rem;
      flex-wrap: wrap;

      .type-label {
        font-weight: 500;
        font-size: 0.875rem;
        color: var(--p-text-secondary-color);
      }

      .type-readable {
        font-weight: 600;
        font-size: 0.9375rem;
        color: var(--p-text-color);
      }

      .type-technical {
        font-family: monospace;
        font-size: 0.8125rem;
        color: var(--p-text-secondary-color);
      }
    }
  }
}
</style>
