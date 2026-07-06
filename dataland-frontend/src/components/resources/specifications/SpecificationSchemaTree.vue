<template>
  <Tree :value="treeNodes" class="specification-schema-tree" data-test="specificationSchemaTree">
    <template #default="slotProps">
      <router-link
        v-if="slotProps.node.data"
        :to="`/specifications/data-point-types/${slotProps.node.data.dataPointTypeId}`"
        class="text-primary font-semibold underline"
        data-test="specificationSchemaTreeLeafLink"
      >
        {{ slotProps.node.label }}
      </router-link>
      <span v-else>{{ slotProps.node.label }}</span>
    </template>
  </Tree>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Tree from 'primevue/tree';
import type { TreeNode } from 'primevue/treenode';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';

const props = defineProps<{
  /**
   * The parsed `schema` of a `FrameworkSpecification` (i.e. `JSON.parse(frameworkSpecification.schema)`):
   * a tree of categories/subcategories whose leaves reference a data point type via `{ id, ref }`.
   */
  schema: Record<string, unknown>;
}>();

interface SchemaLeafReference {
  id: string;
  ref: string;
}

/**
 * Checks whether a schema node is a leaf referencing a data point type (as opposed to a nested category).
 * @param value the schema node to check
 * @returns true if the node is a `{ id, ref }` reference to a data point type
 */
function isSchemaLeafReference(value: unknown): value is SchemaLeafReference {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as Partial<SchemaLeafReference>).id === 'string' &&
    typeof (value as Partial<SchemaLeafReference>).ref === 'string'
  );
}

/**
 * Recursively converts the parsed framework schema into PrimeVue tree nodes. Leaf nodes carry the
 * referenced data point type id so they can link to that data point type's detail page.
 * @param schema the (sub-)tree to convert
 * @param keyPrefix the tree-node key of the parent, used to build unique keys for children
 * @returns the corresponding PrimeVue tree nodes
 */
function buildTreeNodes(schema: Record<string, unknown>, keyPrefix: string): TreeNode[] {
  return Object.entries(schema).map(([fieldName, value]) => {
    const key = keyPrefix ? `${keyPrefix}.${fieldName}` : fieldName;
    if (isSchemaLeafReference(value)) {
      return {
        key,
        label: humanizeStringOrNumber(fieldName),
        leaf: true,
        data: { dataPointTypeId: value.id },
      };
    }
    return {
      key,
      label: humanizeStringOrNumber(fieldName),
      children: buildTreeNodes(value as Record<string, unknown>, key),
    };
  });
}

const treeNodes = computed<TreeNode[]>(() => buildTreeNodes(props.schema, ''));
</script>
