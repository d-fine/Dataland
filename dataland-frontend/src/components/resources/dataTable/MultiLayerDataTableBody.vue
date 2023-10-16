<template>
  <template v-for="(element, idx) in config" :key="idx">
    <template v-if="isElementVisible(element, datasets)">
      <tr v-if="element.type == 'cell'" v-show="isVisible" :data-cell-label="element.label">
        <td class="headers-bg" :data-cell-label="element.label" data-row-header="true">
          <span class="table-left-label">{{ element.label }}</span>
          <em
            v-if="element.explanation"
            class="material-icons info-icon"
            aria-hidden="true"
            :title="element.label"
            v-tooltip.top="{
              value: element.explanation,
            }"
            >info</em
          >
        </td>
        <td
          v-for="(datasetEntry, idx) in datasets"
          :key="idx"
          :data-cell-label="element.label"
          :data-dataset-index="idx"
        >
          <MultiLayerDataTableCell :content="element.valueGetter(datasetEntry.dataset)" />
        </td>
      </tr>
      <template v-else-if="element.type == 'section'">
        <tr
          :class="
            isTopLevel
              ? ['p-rowgroup-header', 'p-topmost-header', 'border-bottom-table']
              : ['p-rowgroup-header', 'border-bottom-table']
          "
          :data-section-label="element.label"
          :data-section-expanded="expandedSections.has(idx)"
          @click="toggleSection(idx)"
          v-show="isVisible"
        >
          <td :colspan="datasets.length + 1">
            <ChevronDownIcon v-if="expandedSections.has(idx)" class="p-icon p-row-toggler-icon absolute right-0 mr-3" />
            <ChevronLeftIcon v-else class="p-icon p-row-toggler-icon absolute right-0 mr-3" />
            <i
              v-if="shouldAddCrossedEyeSymbolToSectionLabel(element)"
              i
              class="pi pi-eye-slash pr-1 text-red-500"
              aria-hidden="true"
              data-test="hidden-icon"
            />
            <span v-if="element.labelBadgeColor" :class="`p-badge badge-${element.labelBadgeColor}`"
              >{{ element.label.toUpperCase() }}
            </span>
            <span v-else class="font-medium">{{ element.label }}</span>
          </td>
        </tr>
        <MultiLayerDataTableBody
          :config="element.children"
          :datasets="datasets"
          :isTopLevel="false"
          :isVisible="isVisible && expandedSections.has(idx)"
        />
      </template>
    </template>
  </template>
</template>

<script setup lang="ts" generic="T">
import {
  isElementVisible,
  type MLDTConfig,
  type MLDTDataset,
  type MLDTSectionConfig,
} from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import ChevronDownIcon from "primevue/icons/chevrondown";
import ChevronLeftIcon from "primevue/icons/chevronleft";
import MultiLayerDataTableBody from "@/components/resources/dataTable/MultiLayerDataTableBody.vue";
import { onMounted, ref } from "vue";
import MultiLayerDataTableCell from "@/components/resources/dataTable/MultiLayerDataTableCell.vue";
import Tooltip from "primevue/tooltip";

const expandedSections = ref(new Set<number>());
const vTooltip = Tooltip;

/**
 * Toggle the visibility of the section at the given index in the configuration
 * @param idx the index of the element whose visibility should be toggled
 */
function toggleSection(idx: number): void {
  if (expandedSections.value.has(idx)) {
    expandedSections.value.delete(idx);
  } else {
    expandedSections.value.add(idx);
  }
}

/**
 * Expand all sections with "expandOnPageLoad" set to true
 */
function expandSectionsOnPageLoad(): void {
  for (let i = 0; i < props.config.length; i++) {
    const element = props.config[i];
    if (element.type == "section" && element.expandOnPageLoad) {
      expandedSections.value.add(i);
    }
  }
}

/**
 * Check if a crossed-eye-symbol shall be added to a section label to express to a reviewer that this section is
 * hidden on the view-page for a normal user.
 * @param element the config for the section that is being validated
 * @returns a boolean stating if the crossed-eye-symbol shall be added to the section label
 */
function shouldAddCrossedEyeSymbolToSectionLabel(element: MLDTSectionConfig<T>): boolean {
  const datasetThatIsBeingReviewed = props.datasets[0].dataset;
  if (element.areThisSectionAndAllParentSectionsDisplayedForTheDataset) {
    return !element.areThisSectionAndAllParentSectionsDisplayedForTheDataset(datasetThatIsBeingReviewed);
  } else return false;
}

const props = defineProps<{
  config: MLDTConfig<T>;
  datasets: Array<MLDTDataset<T>>; // TODO rename later since misleading
  isTopLevel: boolean;
  isVisible: boolean;
}>();

onMounted(() => {
  expandSectionsOnPageLoad();
});
</script>
