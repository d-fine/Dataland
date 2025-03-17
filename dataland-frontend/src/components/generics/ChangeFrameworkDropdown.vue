<template>
  <span data-test="chooseFrameworkDropdown" class="p-dropdown-panel" ref="chooseFrameworkDropdown">
    <span
      @click="dropdownExtended = !dropdownExtended"
      class="fill-dropdown always-fill"
      :class="dropdownExtended ? 'p-overlay-open' : ''"
      :aria-expanded="dropdownExtended"
    >
      <span class="p-dropdown-label p-inputtext p-placeholder">
        {{ humanizeStringOrNumber(dataType) }}
        <span class="p-dropdown-trigger-icon pi pi-angle-down" aria-hidden="true" data-pc-section="dropdownicon"></span>
      </span>
    </span>
    <span v-if="dropdownExtended" class="p-dropdown-trigger p-dropdown-items" data-test="chooseFrameworkList">
      <a
        v-for="option in getFrameworkListSorted(dataMetaInformation)"
        :key="option.label"
        :href="option.value"
        class="p-dropdown-item"
        :class="humanizeStringOrNumber(dataType) === option.label ? ' p-highlight' : ''"
      >
        {{ option.label }}
      </a>
    </span>
  </span>
</template>

<script setup lang="ts">
import { type PropType, ref, onMounted, onBeforeUnmount, useTemplateRef } from 'vue';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type DataMetaInformation } from '@clients/backend';

const props = defineProps({
  companyId: {
    type: String,
    required: true,
  },
  dataType: {
    type: String,
    required: true,
  },
  dataMetaInformation: {
    type: Array as PropType<Array<DataMetaInformation>>,
    required: true,
  },
});

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
});
const element = useTemplateRef('chooseFrameworkDropdown');

/**
 * Close drop down on click outside the element
 * @param event The mouse pointer event anywhere
 */
function handleClickOutside(event: MouseEvent): void {
  if (!element.value?.contains(event?.target as HTMLElement)) {
    dropdownExtended.value = false;
  }
}

const dropdownExtended = ref<boolean>(false);

/**
 * Uses a list of data meta info to derive all distinct frameworks that occur in that list. Only if those distinct
 * frameworks are also included in the frontend constant which contains all frameworks that have view-pages
 * implemented, the distinct frameworks are set as options for the framework-dropdown element.
 * @param dataMetaInformation an array of data meta info
 */
function getFrameworkListSorted(dataMetaInformation: Array<DataMetaInformation>): { label: string; value: string }[] {
  const setOfAvailableFrameworksForCompany = [
    ...new Set(dataMetaInformation.map((individualMetaInfo) => individualMetaInfo.dataType)),
  ];
  const dataTypesInDropdown = setOfAvailableFrameworksForCompany
    .filter((dataType) => FRAMEWORKS_WITH_VIEW_PAGE.includes(dataType))
    .sort((a, b) => a.localeCompare(b))
    .map((dataType) => {
      return {
        label: humanizeStringOrNumber(dataType),
        value: `/companies/${props.companyId}/frameworks/${dataType}`,
      };
    });
  dataTypesInDropdown.push({ label: 'Documents', value: `/companies/${props.companyId}/documents` });
  return dataTypesInDropdown;
}
</script>

<style scoped>
.p-dropdown-panel {
  position: relative;
}

.fill-dropdown {
  padding: 0.5rem;
  display: inline-block;
  cursor: pointer;
  user-select: none;
}

.p-dropdown-items {
  width: fit-content;
  display: block;
  position: absolute;
  background-color: #ffffff;
}

.p-dropdown-item {
  display: block;
  white-space: nowrap;
  text-decoration: none;
}

.p-dropdown-trigger-icon {
  margin-left: 1rem;
}
</style>
