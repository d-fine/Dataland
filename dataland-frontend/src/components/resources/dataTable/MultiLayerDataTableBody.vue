<template>
  <template v-for="(cellOrSectionConfig, idx) in config" :key="idx">
    <template v-if="isCellOrSectionVisible(cellOrSectionConfig, mldtDatasets)">
      <tr v-if="cellOrSectionConfig.type == 'cell'" v-show="isVisible" :data-cell-label="cellOrSectionConfig.label">
        <td class="headers-bg" :data-cell-label="cellOrSectionConfig.label" data-row-header="true">
          <span class="table-left-label">{{ cellOrSectionConfig.label }}</span>
          <em
            v-if="cellOrSectionConfig.explanation"
            class="material-icons info-icon"
            aria-hidden="true"
            :title="cellOrSectionConfig.label"
            v-tooltip.top="{
              value: cellOrSectionConfig.explanation,
            }"
            >info</em
          >
        </td>
        <td
          v-for="(mldtDataset, idx) in mldtDatasets"
          :key="idx"
          :data-cell-label="cellOrSectionConfig.label"
          :data-dataset-index="idx"
        >
          <MultiLayerDataTableCell :content="cellOrSectionConfig.valueGetter(mldtDataset.dataset)" />
        </td>
      </tr>
      <template v-else-if="cellOrSectionConfig.type == 'section'">
        <tr
          :class="
            isTopLevel
              ? ['p-rowgroup-header', 'p-topmost-header', 'border-bottom-table']
              : ['p-rowgroup-header', 'border-bottom-table']
          "
          :data-section-label="cellOrSectionConfig.label"
          :data-section-expanded="expandedSections.has(idx)"
          @click="toggleSection(idx)"
          v-show="isVisible"
        >
          <td :colspan="mldtDatasets.length + 1">
            <ChevronDownIcon v-if="expandedSections.has(idx)" class="p-icon p-row-toggler-icon absolute right-0 mr-3" />
            <ChevronLeftIcon v-else class="p-icon p-row-toggler-icon absolute right-0 mr-3" />
            <i
              v-if="shouldAddCrossedEyeSymbolToSectionLabel(cellOrSectionConfig)"
              i
              class="pi pi-eye-slash pr-1 text-red-500"
              aria-hidden="true"
              data-test="hidden-icon"
            />
            <span
              v-if="cellOrSectionConfig.labelBadgeColor"
              :class="`p-badge badge-${cellOrSectionConfig.labelBadgeColor}`"
              >{{ cellOrSectionConfig.label.toUpperCase() }}
            </span>
            <span v-else class="font-medium">{{ cellOrSectionConfig.label }}</span>
          </td>
        </tr>
        <MultiLayerDataTableBody
          :config="cellOrSectionConfig.children"
          :mldtDatasets="mldtDatasets"
          :isTopLevel="false"
          :isVisible="isVisible && expandedSections.has(idx)"
        />
      </template>
    </template>
  </template>
</template>

<script setup lang="ts" generic="T">
import {
  isCellOrSectionVisible,
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
  const datasetThatIsBeingReviewed = props.mldtDatasets[0].dataset;
  if (element.areThisSectionAndAllParentSectionsDisplayedForTheDataset) {
    return !element.areThisSectionAndAllParentSectionsDisplayedForTheDataset(datasetThatIsBeingReviewed);
  } else return false;
}

const props = defineProps<{
  config: MLDTConfig<T>;
  mldtDatasets: Array<MLDTDataset<T>>;
  isTopLevel: boolean;
  isVisible: boolean;
}>();

onMounted(() => {
  expandSectionsOnPageLoad();
});
</script>
