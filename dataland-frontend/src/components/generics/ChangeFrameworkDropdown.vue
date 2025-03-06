<template>
  <span data-test="chooseFrameworkDropdown" class="p-dropdown-panel">
    <span
      @click="toggleDropdown"
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
        v-for="option in getDistinctAvailableFrameworksAndPutThemSortedIntoDropdown(listOfDataMetaInfo)"
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

<script lang="ts">
import { defineComponent, type PropType, ref } from 'vue';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import type { DataAndMetaInformation } from '@/api-models/DataAndMetaInformation.ts';
import type { FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { type DataTypeEnumAndDocumentsEntry } from '@/types/DataTypeEnumAndDocumentsEntry.ts';

const dropdownExtended = ref<boolean>(false);

export default defineComponent({
  name: 'ChangeFrameworkDropdown',
  components: {},
  props: {
    companyID: {
      type: String,
      required: true,
    },
    dataType: {
      type: String as PropType<DataTypeEnumAndDocumentsEntry>,
      required: true,
    },
    listOfDataMetaInfo: {
      type: Array as PropType<Array<DataAndMetaInformation<FrameworkData>>>,
      required: true,
    },
  },
  data() {
    return {
      humanizeStringOrNumber,
      dropdownExtended,
    };
  },
  methods: {
    /** Expands or collapses the drop-down. **/
    toggleDropdown() {
      dropdownExtended.value = !dropdownExtended.value;
    },

    /**
     * Uses a list of data meta info to derive all distinct frameworks that occur in that list. Only if those distinct
     * frameworks are also included in the frontend constant which contains all frameworks that have view-pages
     * implemented, the distinct frameworks are set as options for the framework-dropdown element.
     * @param listOfDataMetaInfo a list of data meta info
     */
    getDistinctAvailableFrameworksAndPutThemSortedIntoDropdown(
      listOfDataMetaInfo: Array<DataAndMetaInformation<FrameworkData>>
    ) {
      const setOfAvailableFrameworksForCompany = [
        ...new Set(listOfDataMetaInfo.map((dataAndMetaInfo) => dataAndMetaInfo.metaInfo.dataType)),
      ];
      const dataTypesInDropdown = setOfAvailableFrameworksForCompany
        .filter((dataType) => FRAMEWORKS_WITH_VIEW_PAGE.includes(dataType))
        .sort((a, b) => a.localeCompare(b))
        .map((dataType) => {
          return {
            label: humanizeStringOrNumber(dataType),
            value: `/companies/${this.companyID}/frameworks/${dataType}`,
          };
        });
      dataTypesInDropdown.push({ label: 'Documents', value: `/companies/${this.companyID}/documents` });
      return dataTypesInDropdown;
    },
  },
});
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
