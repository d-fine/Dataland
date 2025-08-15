<template>
  <div class="schema-node">
    <!-- Data Point Card (leaf node with id, ref, aliasExport) - Now retractable -->
    <div v-if="isDataPoint">
      <div @click="toggleExpanded" class="data-point-header-clickable" :style="{ paddingLeft: `${depth * 16}px` }">
        <i :class="isExpanded ? 'pi pi-chevron-down' : 'pi pi-chevron-right'" class="chevron-icon"></i>
        <strong class="data-point-title">{{ currentKey }}</strong>
      </div>

      <div v-show="isExpanded" class="data-point-content-expandable">
        <div class="data-point-field">
          <strong>ID:</strong>
          <code>{{ data.id }}</code>
        </div>
        <div v-if="data.ref" class="data-point-field">
          <strong>Reference:</strong>
          <a
            v-if="isInternalRoute(data.ref)"
            :href="convertToDocumentationRoute(data.ref)"
            target="_blank"
            rel="noopener noreferrer"
            class="data-point-link internal-link"
            @mouseover="($event.target as HTMLElement).style.textDecoration = 'underline'"
            @mouseout="($event.target as HTMLElement).style.textDecoration = 'none'"
          >
            {{ convertToDocumentationUrl(data.ref) }}
          </a>
          <a
            v-else
            :href="data.ref"
            target="_blank"
            class="data-point-link"
            @mouseover="($event.target as HTMLElement).style.textDecoration = 'underline'"
            @mouseout="($event.target as HTMLElement).style.textDecoration = 'none'"
          >
            {{ data.ref }}
          </a>
        </div>
        <div v-if="data.aliasExport" class="data-point-field">
          <strong>Alias Export:</strong>
          <code>{{ data.aliasExport }}</code>
        </div>
      </div>
    </div>

    <!-- Expandable Section (internal nodes) -->
    <div v-else>
      <div @click="toggleExpanded" class="schema-section-header" :style="{ paddingLeft: `${depth * 16}px` }">
        <i :class="isExpanded ? 'pi pi-chevron-down' : 'pi pi-chevron-right'" class="chevron-icon"></i>
        <strong class="section-title">{{ currentKey }}</strong>
        <span class="item-count">({{ Object.keys(data).length }} items)</span>
      </div>

      <div v-show="isExpanded" class="schema-section-content">
        <SchemaNode
          v-for="[key, value] in Object.entries(data)"
          :key="key"
          :data="value"
          :path="[...path, key]"
          :expanded-nodes="expandedNodes"
          :depth="depth + 1"
          :framework-name="frameworkName"
          @toggle="$emit('toggle', $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {
  convertSpecificationUrlToDocumentationRoute,
  convertSpecificationUrlToDocumentationUrl,
  isSpecificationServiceUrl,
} from '@/utils/DocumentationUrlConverter';

interface Props {
  data: any;
  path: string[];
  expandedNodes: Record<string, boolean>;
  depth?: number;
  frameworkName?: string;
}

interface Emits {
  (e: 'toggle', path: string[]): void;
}

const props = withDefaults(defineProps<Props>(), {
  depth: 0,
});

const emit = defineEmits<Emits>();

const currentKey = computed(() => {
  if (props.path.length === 0) {
    return props.frameworkName || 'root';
  }
  return props.path[props.path.length - 1];
});

const pathKey = computed(() => {
  return props.path.join('.');
});

const isExpanded = computed(() => {
  return props.expandedNodes[pathKey.value] || false;
});

const isDataPoint = computed(() => {
  return (
    typeof props.data === 'object' && props.data !== null && 'id' in props.data && typeof props.data.id === 'string'
  );
});

const toggleExpanded = () => {
  emit('toggle', props.path);
};

const isInternalRoute = (url: string) => {
  return isSpecificationServiceUrl(url);
};

const convertToDocumentationRoute = (url: string) => {
  return convertSpecificationUrlToDocumentationRoute(url);
};

const convertToDocumentationUrl = (url: string) => {
  return convertSpecificationUrlToDocumentationUrl(url);
};
</script>

<style scoped>
/* Card-like structure for schema nodes */
.data-point-header-clickable,
.schema-section-header {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: var(--spacing-sm);
  border: 1px solid var(--table-border);
  border-radius: 4px;
  background-color: var(--card-background);
  margin-bottom: var(--spacing-xs);
}

.data-point-header-clickable:hover,
.schema-section-header:hover {
  background-color: var(--table-background-hover-color);
}

/* Card-like expandable content */
.data-point-content-expandable {
  margin-left: var(--spacing-md);
  padding: var(--spacing-sm);
  border: 1px solid var(--table-border);
  border-radius: 4px;
  background-color: var(--tables-headers-bg);
  margin-bottom: var(--spacing-xs);
}

.schema-section-content {
  margin-left: var(--spacing-md);
}
</style>
