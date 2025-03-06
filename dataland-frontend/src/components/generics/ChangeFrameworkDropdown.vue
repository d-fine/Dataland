<template>
  <Dropdown
    id="chooseFrameworkDropdown"
    :options="getDistinctAvailableFrameworksAndPutThemSortedIntoDropdown(listOfDataMetaInfo)"
    optionLabel="label"
    optionValue="value"
    :placeholder="humanizeStringOrNumber(dataType)"
    aria-label="Choose framework or documents"
    class="fill-dropdown always-fill"
    dropdownIcon="pi pi-angle-down"
    @change="changeUrl"
    data-test="chooseFrameworkDropdown"
  />
</template>

<script lang="ts">
import Dropdown, { type DropdownChangeEvent } from 'primevue/dropdown';
import { defineComponent, type PropType } from 'vue';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type DataMetaInformation } from '@clients/backend';
import router from '@/router';
import { type DataTypeEnumAndDocumentsEntry } from '@/types/DataTypeEnumAndDocumentsEntry.ts';

export default defineComponent({
  name: 'ChangeFrameworkDropdown',
  components: {
    Dropdown,
  },
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
      type: Array as PropType<DataMetaInformation[]>,
      required: true,
    },
  },
  data() {
    return {
      humanizeStringOrNumber,
    };
  },
  methods: {
    /**
     * Visits the page, which was chosen in the dropdown
     * @param event the change event emitted by the dropdown component
     */
    changeUrl(event: DropdownChangeEvent) {
      return router.push(event.value);
    },

    /**
     * Uses a list of data meta info to derive all distinct frameworks that occur in that list. Only if those distinct
     * frameworks are also included in the frontend constant which contains all frameworks that have view-pages
     * implemented, the distinct frameworks are set as options for the framework-dropdown element.
     * @param listOfDataMetaInfo a list of data meta info
     */
    getDistinctAvailableFrameworksAndPutThemSortedIntoDropdown(listOfDataMetaInfo: DataMetaInformation[]) {
      const setOfAvailableFrameworksForCompany = [
        ...new Set(listOfDataMetaInfo.map((dataMetaInfo) => dataMetaInfo.dataType)),
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
