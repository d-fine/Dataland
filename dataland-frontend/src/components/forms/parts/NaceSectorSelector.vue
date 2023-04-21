<template>
  <InputText v-model="searchFilter" placeholder="Search for NACE Rev2 codes" @focus="inputFocused"></InputText>
  <OverlayPanel ref="overlayPanel">
    <div class="d-nace-treeview-container">
      <Tree v-model:expanded-keys="expandedTreeKeys" :value="filteredTreeValues" placeholder="Select Item">
        <template #default="slotProps">
          <div class="flex align-items-center">
            <PrimeButton
              v-if="!isSelected(slotProps.node.key)"
              icon="pi pi-plus"
              severity="success"
              text
              rounded
              aria-label="Add"
              @click="selectNode(slotProps.node.key)"
            ></PrimeButton>
            <PrimeButton
              v-else
              icon="pi pi-times"
              severity="danger"
              text
              rounded
              aria-label="Remove"
              @click="unselectNode(slotProps.node.key)"
            ></PrimeButton>
            <span>{{ slotProps.node.label }}</span>
          </div>
        </template>
      </Tree>
    </div>
  </OverlayPanel>
  <div class="pt-1">
    <span class="form-list-item" :key="element" v-for="element in modelValue">
      {{ element }}
      <em @click="unselectNode(element)" class="material-icons">close</em>
    </span>
  </div>
</template>

<script lang="ts">
import Tree, { TreeNode } from "primevue/tree";
import InputText from "primevue/inputtext";
import OverlayPanel from "primevue/overlaypanel";
import PrimeButton from "primevue/button";

import { naceData, filterNodes } from "@/components/forms/parts/NaceData";
import { defineComponent, PropType, ref } from "vue";

export default defineComponent({
  name: "NaceSectorSelector",
  emits: ["update:modelValue"],
  props: {
    modelValue: {
      type: Array as PropType<Array<string>>,
      default: () => [],
    },
  },
  components: { Tree, OverlayPanel, InputText, PrimeButton },
  setup() {
    return {
      overlayPanel: ref<OverlayPanel>(),
    };
  },
  methods: {
    /**
     * Executed, whenever the search bar input is focused. Opens the Tree Overlay.
     * @param event the onclick event
     */
    inputFocused(event: Event) {
      this.overlayPanel.show(event);
    },
    /**
     * Marks a treeNode as selected given its key
     * @param key the key of the node
     */
    selectNode(key: string) {
      const selectedNodes = this.modelValue || [];
      selectedNodes.push(key);
      this.$emit("update:modelValue", selectedNodes);
    },
    /**
     * Deselects a treeNode when it has been previously selected.
     * When the given node is not selected, nothing happens.
     * @param key the key of the node
     */
    unselectNode(key: string) {
      const selectedNodes = this.modelValue || [];
      const idx = selectedNodes.indexOf(key);
      if (idx > -1) {
        selectedNodes.splice(idx, 1);
      }
      this.$emit("update:modelValue", selectedNodes);
    },
    /**
     * Checks if the node identified by the provided key is selected or not
     * @param key the key of the node to check
     * @returns true iff the given node is selected, false otherwise
     */
    isSelected(key: string): boolean {
      const selectedNodes = this.modelValue || [];
      const idx = selectedNodes.indexOf(key);
      return idx > -1;
    },
    /**
     * Recursively expands the given nodes and all its children
     * @param node the node to expand
     * @param dict the dict to store the keys of the expanded nodes to
     */
    expandNode(node: TreeNode, dict: { [id: string]: boolean }) {
      dict[node.key] = true;
      for (const child of node.children) {
        this.expandNode(child, dict);
      }
    },
  },
  data() {
    return {
      filteredTreeValues: naceData,
      searchFilter: "",
      expandedTreeKeys: {},
    };
  },
  watch: {
    searchFilter(searchFilter: string) {
      if (searchFilter) {
        const copy = structuredClone(naceData) as Array<TreeNode>;
        this.filteredTreeValues = filterNodes(copy, searchFilter);
        const expandedKeysDict = {};
        for (const node of this.filteredTreeValues) {
          this.expandNode(node, expandedKeysDict);
        }
        this.expandedTreeKeys = expandedKeysDict;
      } else {
        this.expandedTreeKeys = {};
        this.filteredTreeValues = naceData;
      }
    },
  },
});
</script>

<style scoped>
.d-nace-treeview-container {
  max-height: 400px;
  overflow: auto;
}
</style>
