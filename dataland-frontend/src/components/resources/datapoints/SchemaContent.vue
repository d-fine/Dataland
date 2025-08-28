<template>
  <div
    :style="{
      backgroundColor: 'var(--p-indigo-50)',
      padding: 'var(--spacing-sm)',
      borderRadius: 'var(--p-border-radius-xs)',
      borderLeft: 'var(--spacing-xxxs) solid var(--p-indigo-400)',
    }"
  >
    <div style="font-size: var(--font-size-sm); color: var(--p-indigo-800)">
      <strong>Schema Structure:</strong>
      <div style="margin-top: var(--spacing-sm)">
        <SchemaNode
          v-if="parsedSchema"
          :data="parsedSchema"
          :path="[]"
          :expanded-nodes="expandedNodes"
          :framework-name="frameworkName"
          @toggle="toggleNode"
        />
        <div v-else style="color: var(--p-red-700); font-style: italic">Invalid JSON schema</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import SchemaNode from '@/components/resources/datapoints/SchemaNode.vue';

interface Props {
  schema: string;
  frameworkName?: string;
}

const props = defineProps<Props>();
const expandedNodes = ref<Record<string, boolean>>({});

const parsedSchema = computed(() => {
  try {
    return JSON.parse(props.schema);
  } catch {
    return null;
  }
});

const toggleNode = (path: string[]) => {
  const key = path.join('.');
  expandedNodes.value[key] = !expandedNodes.value[key];
};
</script>
