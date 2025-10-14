<template>
  <template v-for="(cellOrSectionConfig, idx) in config" :key="idx">
    <template v-if="isCellOrSectionVisible(cellOrSectionConfig, dataAndMetaInfo)">
      <tr
        v-if="cellOrSectionConfig.type == 'cell'"
        v-show="isVisible"
        :data-cell-label="cellOrSectionConfig.label"
        :class="cellOrSectionConfig.class ?? null"
      >
        <td
          class="headers-bg pl-4 vertical-align-top header-column-width"
          :data-cell-label="cellOrSectionConfig.label"
          data-row-header="true"
        >
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
          v-for="(sinlgeDataAndMetaInfo, idx) in dataAndMetaInfo"
          :key="idx"
          :data-cell-label="cellOrSectionConfig.label"
          :data-dataset-index="idx"
          :style="columnWidthStyle"
          class="vertical-align-top"
        >
          <MultiLayerDataTableCell
            :content="cellOrSectionConfig.valueGetter(sinlgeDataAndMetaInfo.data)"
            :meta-info="sinlgeDataAndMetaInfo.metaInfo"
            :inReviewMode="inReviewMode"
          />
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
          <td :colspan="dataAndMetaInfo.length + 1" :class="isTopLevel ? 'pl-2' : null">
            <i
              v-if="expandedSections.has(idx)"
              class="pi pi-chevron-down p-icon p-row-toggler-icon absolute right-0 mr-3"
            />
            <i v-else class="pi pi-chevron-left p-icon p-row-toggler-icon absolute right-0 mr-3" />
            <i
              v-if="shouldAddCrossedEyeSymbolToSectionLabel(cellOrSectionConfig) && inReviewMode"
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
          :dataAndMetaInfo="dataAndMetaInfo"
          :isTopLevel="false"
          :isVisible="isVisible && expandedSections.has(idx)"
          :inReviewMode="inReviewMode"
        />
      </template>
    </template>
  </template>
</template>

<script setup lang="ts" generic="T">
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
import MultiLayerDataTableBody from '@/components/resources/dataTable/MultiLayerDataTableBody.vue';
import MultiLayerDataTableCell from '@/components/resources/dataTable/MultiLayerDataTableCell.vue';
import {
  isCellOrSectionVisible,
  type MLDTConfig,
  type MLDTSectionConfig,
} from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import Tooltip from 'primevue/tooltip';
import { computed, onMounted, ref } from 'vue';

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
    if (element && element.type == 'section' && element.expandOnPageLoad) {
      expandedSections.value.add(i);
    }
  }
}

const columnWidthStyle = computed(() => {
  return `width: ${70 / props.dataAndMetaInfo.length}%`;
});

/**
 * Check if a crossed-eye-symbol shall be added to a section label to express to a reviewer that this section is
 * hidden on the view-page for a normal user.
 * @param element the config for the section that is being validated
 * @returns a boolean stating if the crossed-eye-symbol shall be added to the section label
 */
function shouldAddCrossedEyeSymbolToSectionLabel(element: MLDTSectionConfig<T>): boolean {
  if (!props.dataAndMetaInfo[0]) return false;
  const datasetThatIsBeingReviewed = props.dataAndMetaInfo[0].data;
  if (element.areThisSectionAndAllParentSectionsDisplayedForTheDataset) {
    return !element.areThisSectionAndAllParentSectionsDisplayedForTheDataset(datasetThatIsBeingReviewed);
  } else return false;
}

const props = defineProps<{
  config: MLDTConfig<T>;
  dataAndMetaInfo: Array<DataAndMetaInformation<T>>;
  isTopLevel: boolean;
  isVisible: boolean;
  inReviewMode: boolean;
}>();

onMounted(() => {
  expandSectionsOnPageLoad();
});
</script>

<style scoped>
.info-icon {
  cursor: help;
}

.vertical-align-top {
  vertical-align: top;
}
.header-column-width {
  width: 30%;
}
</style>
