<template>
  <div class="flex align-items-end">
    <InputText
      v-model="searchFilter"
      placeholder="Search for NACE Rev 2 Codes"
      @focus="inputFocused"
      class="p-multiselect short d-nace-focus"
    ></InputText>
    <div class="pl-1 d-nace-chipview">
      <span
        class="form-list-item"
        v-tooltip.top="getNodeLabel(naceCode)"
        :key="naceCode"
        v-for="naceCode in modelValue"
      >
        {{ naceCode }}
        <em @click="this.selectedTreeNodes.delete(naceCode)" class="material-icons">close</em>
      </span>
    </div>
  </div>

  <OverlayPanel ref="overlayPanel">
    <div class="d-nace-treeview-container">
      <h2 v-if="filteredTreeValues.length <= 0">No results</h2>
      <Tree v-model:expanded-keys="expandedTreeKeys" :value="filteredTreeValues" placeholder="Select Item">
        <template #default="slotProps">
          <div class="flex align-items-center">
            <Checkbox
              :modelValue="selectedTreeNodes.has(slotProps.node.key)"
              @update:modelValue="(isChecked: boolean) => handleNodeCheckboxClick(slotProps.node.key, isChecked)"
              :binary="true"
            ></Checkbox>
            <div :class="{ invisible: !selectedChildrenCounter.get(slotProps.node.key) }">
              <span class="p-badge p-badge-no-gutter">{{ selectedChildrenCounter.get(slotProps.node.key) || 0 }}</span>
            </div>
            <span>{{ slotProps.node.label }}</span>
          </div>
        </template>
      </Tree>
    </div>
  </OverlayPanel>
</template>

<script lang="ts">
import Tree, { TreeNode } from "primevue/tree";
import InputText from "primevue/inputtext";
import OverlayPanel from "primevue/overlaypanel";
import Checkbox from "primevue/checkbox";

import { naceCodeTree, naceCodeMap, filterNodes } from "@/components/forms/parts/elements/derived/NaceCodeTree";
import Tooltip from "primevue/tooltip";
import { defineComponent, PropType, ref } from "vue";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "NaceCodeSelector",
  emits: ["update:modelValue"],
  directives: {
    tooltip: Tooltip,
  },
  props: {
    modelValue: {
      type: Array as PropType<Array<string>>,
      default: () => [],
    },
  },
  components: { Tree, OverlayPanel, InputText, Checkbox },
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
     * Handles the click event of the selection checkboxes
     * @param key The key of the node whose checkbox has been clicked
     * @param isChecked the new value of the checkbox
     */
    handleNodeCheckboxClick(key: string, isChecked: boolean) {
      if (isChecked) this.selectedTreeNodes.add(key);
      else this.selectedTreeNodes.delete(key);
    },
    /**
     * Recursively expands the given nodes and all its children up to a maximum of max expanded nodes
     * @param node the node to expand
     * @param dict the dict to store the keys of the expanded nodes to
     */
    expandNode(node: TreeNode, dict: { [id: string]: boolean }) {
      // Cap expanded nodes to 100 to prevent performance issues that would arise when
      // e.g. someone searches for "a" and everything expands
      if (Object.keys(dict).length >= 100) return;
      dict[node.key] = true;
      for (const child of node.children) {
        this.expandNode(child, dict);
      }
    },
    /**
     * Recalculates the selectedChildrenCounters for all elements based on the selectedNodeSet
     * @param selectedTreeNodeSet the set of selected nodes
     */
    updateSelectedChildrenCounter(selectedTreeNodeSet: Set<string>) {
      const newSelectedChildrenCounter = new Map<string, number>();
      const childrenCounterPopulator = (node: TreeNode): void => {
        const children = node.children ?? [];
        let localSum = 0;
        children.forEach((child) => {
          if (selectedTreeNodeSet.has(assertDefined(child.key))) localSum++;
          childrenCounterPopulator(child);
          localSum += newSelectedChildrenCounter.get(assertDefined(child.key));
        });
        newSelectedChildrenCounter.set(assertDefined(node.key), localSum);
      };
      naceCodeTree.forEach((treeNode) => {
        childrenCounterPopulator(treeNode);
      });
      this.selectedChildrenCounter = newSelectedChildrenCounter;
    },
    /**
     * Emits an update for the modelValue based on the selectedNodeSet
     * @param selectedTreeNodeSet the set of selected nodes
     */
    updateModelValue(selectedTreeNodeSet: Set<string>) {
      const newModelValue = [...selectedTreeNodeSet].sort((a, b) => a.localeCompare(b));
      this.$emit("update:modelValue", newModelValue);
    },
    /**
     * Returns the label of a node with the provided NACE code
     * @param key the NACE code to lookup
     * @returns the label of the NACE code
     */
    getNodeLabel(key: string): string | undefined {
      return naceCodeMap.get(key).label;
    },
  },
  data() {
    return {
      filteredTreeValues: naceCodeTree,
      searchFilter: "",
      expandedTreeKeys: {},
      selectedTreeNodes: new Set<string>(),
      selectedChildrenCounter: new Map<string, number>(),
    };
  },
  watch: {
    searchFilter(searchFilter: string) {
      if (searchFilter) {
        const copy = structuredClone(naceCodeTree) as Array<TreeNode>;
        this.filteredTreeValues = filterNodes(copy, searchFilter);
        const expandedKeysDict = {};
        for (const node of this.filteredTreeValues) {
          this.expandNode(node, expandedKeysDict);
        }
        this.expandedTreeKeys = expandedKeysDict;
      } else {
        this.expandedTreeKeys = {};
        this.filteredTreeValues = naceCodeTree;
      }
    },
    selectedTreeNodes: {
      deep: true,
      handler(newValue: Set<string>) {
        this.updateSelectedChildrenCounter(newValue);
        this.updateModelValue(newValue);
      },
    },
    modelValue: {
      deep: true,
      handler(newValue: Array<string>) {
        const newlySelectedValues = new Set(newValue);
        const currentlySelectedValues = [...this.selectedTreeNodes];

        currentlySelectedValues.forEach((element) => {
          if (!newlySelectedValues.has(element)) {
            this.selectedTreeNodes.delete(element);
          }
        });

        newlySelectedValues.forEach((it) => {
          this.selectedTreeNodes.add(it);
        });
      },
    },
  },
});
</script>

<style scoped>
.d-nace-treeview-container {
  height: 400px;
  min-width: 200px;
  overflow: auto;
}
.invisible {
  visibility: hidden;
}
.d-nace-focus {
  &:focus {
    box-shadow: none;
  }
}
.d-nace-chipview {
  max-width: 66%;
}
</style>
