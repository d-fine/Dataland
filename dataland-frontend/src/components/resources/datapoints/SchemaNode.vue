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
          <router-link
            v-if="isInternalRoute(data.ref)"
            :to="convertToDocumentationRoute(data.ref)"
            class="data-point-link internal-link"
            @mouseover="($event.target as HTMLElement).style.textDecoration = 'underline'"
            @mouseout="($event.target as HTMLElement).style.textDecoration = 'none'"
          >
            {{ convertToDocumentationRoute(data.ref) }}
          </router-link>
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
import { convertSpecificationUrlToDocumentationRoute, isSpecificationServiceUrl } from '@/utils/DocumentationUrlConverter';

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
</script>

<style scoped>
.schema-node {
  margin-bottom: var(--spacing-xs);
}

/* Data Point Retractable Header Styling */
.data-point-header-clickable {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm);
  cursor: pointer;
  background-color: var(--p-indigo-200);
  border: 1px solid var(--p-indigo-300);
  border-radius: var(--p-border-radius-xs);
  margin-bottom: var(--spacing-xs);
  transition: background-color 0.15s ease-in-out;
  font-size: var(--font-size-sm);
}

.data-point-header-clickable:hover {
  background-color: var(--p-indigo-250);
}

.data-point-title {
  color: var(--p-indigo-900);
}

.data-point-content-expandable {
  background-color: var(--p-indigo-100);
  border: 1px solid var(--p-indigo-300);
  border-radius: var(--p-border-radius-xs);
  padding: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
  margin-left: var(--spacing-md);
  border-left: 2px solid var(--p-indigo-300);
}

.data-point-field {
  margin-bottom: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: var(--p-indigo-800);
}

.data-point-field:last-child {
  margin-bottom: 0;
}

.data-point-field code {
  background-color: var(--p-indigo-50);
  padding: var(--spacing-xxxs) var(--spacing-xs);
  border-radius: var(--p-border-radius-xs);
  font-family: monospace;
  font-size: var(--font-size-xs);
  margin-left: var(--spacing-xs);
}

.data-point-link {
  color: var(--p-indigo-700);
  margin-left: var(--spacing-xs);
  word-break: break-all;
  font-size: var(--font-size-xs);
  transition: color 0.15s ease-in-out;
}

.data-point-link:hover {
  color: var(--p-indigo-900);
}

.data-point-link.internal-link {
  color: var(--p-indigo-700);
}

.data-point-link.internal-link:hover {
  color: var(--p-indigo-900);
}

/* Schema Section Styling */
.schema-section-header {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm);
  cursor: pointer;
  background-color: var(--p-indigo-100);
  border: 1px solid var(--p-indigo-200);
  border-radius: var(--p-border-radius-xs);
  margin-bottom: var(--spacing-xs);
  transition: background-color 0.15s ease-in-out;
  font-size: var(--font-size-sm);
}

.schema-section-header:hover {
  background-color: var(--p-indigo-150);
}

.chevron-icon {
  font-size: var(--font-size-sm);
  color: var(--p-indigo-600);
  margin-right: var(--spacing-sm);
  flex-shrink: 0;
  line-height: 1;
  width: var(--spacing-md);
  text-align: center;
}

.section-title {
  color: var(--p-indigo-900);
  margin-right: var(--spacing-sm);
  flex-grow: 1;
}

.item-count {
  color: var(--p-indigo-600);
  font-size: var(--font-size-xs);
  font-weight: normal;
}

.schema-section-content {
  margin-left: var(--spacing-md);
  border-left: 2px solid var(--p-indigo-200);
  padding-left: var(--spacing-sm);
}
</style>
