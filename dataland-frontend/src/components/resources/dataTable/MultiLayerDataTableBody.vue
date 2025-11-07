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
            :data-test="'edit-data-point-' + cellOrSectionConfig.dataPointTypeId"
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
          <div class="flex items-center gap-2">
            <MultiLayerDataTableCell
              :content="cellOrSectionConfig.valueGetter(sinlgeDataAndMetaInfo.data)"
              :meta-info="sinlgeDataAndMetaInfo.metaInfo"
              :inReviewMode="inReviewMode"
            />
            <PrimeButton
              class="p-0 h-auto"
              v-if="editModeIsOn"
              icon="pi pi-pencil"
              :data-test="'edit-data-point-icon-' + cellOrSectionConfig.dataPointTypeId"
              variant="text"
              :disabled="!isEditComponentAvailable(cellOrSectionConfig.uploadComponentName)"
              v-tooltip.top="
                isEditComponentAvailable(cellOrSectionConfig.uploadComponentName)
                  ? 'Edit Data Point'
                  : 'Currently not implemented'
              "
              @click.stop="openEditDataPointModal(idx, cellOrSectionConfig)"
              :pt="{
                root: {
                  class: isEditComponentAvailable(cellOrSectionConfig.uploadComponentName)
                    ? 'text-primary hover:text-primary-600'
                    : 'text-gray-400 cursor-not-allowed',
                  'data-dpt-id': cellOrSectionConfig.dataPointTypeId,
                },
                icon: {
                  class: isEditComponentAvailable(cellOrSectionConfig.uploadComponentName)
                    ? 'text-inherit'
                    : 'text-gray-400',
                },
              }"
            />
          </div>
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
          @dataUpdated="$emit('dataUpdated')"
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
  type MLDTCellConfig,
  type MLDTConfig,
  type MLDTSectionConfig,
} from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import Tooltip from 'primevue/tooltip';
import { computed, inject, nextTick, onMounted, ref } from 'vue';
import PrimeButton from 'primevue/button';
import { useDialog } from 'primevue/usedialog';
import EditDataPointDialog from '@/components/resources/dataTable/modals/EditDataPointDialog.vue';
import { componentDictionary } from '@/components/resources/dataTable/EditDataPointComponentDictionary.ts';

const expandedSections = ref(new Set<number>());
const vTooltip = Tooltip;
const editModeIsOn = inject('editModeIsOn');
const dialog = useDialog();
const emit = defineEmits<{
  dataUpdated: [];
}>();

/**
 * Scrolls to a specific target element identified by its unique data attribute if it becomes visible.
 */
async function scroller(targetId?: string): Promise<void> {
  if (!targetId) return;
  await scrollToWhenVisible(`[data-dpt-id="${targetId}"]`);
}

/**
 * Scrolls to the first visible element matching the specified CSS selector within a given timeout.
 * Continuously checks if the element becomes visible during the timeout period.
 */
async function scrollToWhenVisible(selector: string, timeout = 5000): Promise<void> {
  const start = performance.now();

  while (performance.now() - start < timeout) {
    await new Promise((r) => setTimeout(r, 100));

    const el = document.querySelector<HTMLElement>(selector);
    if (el && el.offsetParent !== null) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }
  }
}

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

/**
 * Check if an edit component is available for the given uploadComponentName
 * @param uploadComponentName the name of the component to check
 * @returns true if the component exists in the dictionary, false otherwise
 */
function isEditComponentAvailable(uploadComponentName?: string): boolean {
  if (!uploadComponentName) return false;
  return uploadComponentName in componentDictionary;
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

/**
 * Opens a modal dialog for editing a data point.
 */
function openEditDataPointModal(idx: number, cellOrSectionConfig: MLDTCellConfig<T>): void {
  const reportingPeriod = props.dataAndMetaInfo[idx]?.metaInfo.reportingPeriod;
  const companyId = props.dataAndMetaInfo[idx]?.metaInfo.companyId;
  const dataType = props.dataAndMetaInfo[idx]?.metaInfo.dataType;
  const data = props.dataAndMetaInfo[idx]?.data;
  const dataPoint = data == null ? undefined : cellOrSectionConfig.valueGetter(data);
  dialog.open(EditDataPointDialog, {
    props: {
      modal: true,
      header: 'Edit Data Point',
      pt: {
        title: {
          style: {
            maxWidth: '20em',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          },
        },
      },
    },
    data: {
      companyId: companyId,
      reportingPeriod: reportingPeriod,
      uploadComponentName: cellOrSectionConfig.uploadComponentName,
      dataPointTypeId: cellOrSectionConfig.dataPointTypeId,
      dataType: dataType,
      dataPoint: dataPoint,
    },
    onClose: (options) => {
      void (async (): Promise<void> => {
        if (options?.data?.dataUpdated) {
          emit('dataUpdated');
          await nextTick();
          await scroller(cellOrSectionConfig.dataPointTypeId);
        }
      })();
    },
  });
}
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
