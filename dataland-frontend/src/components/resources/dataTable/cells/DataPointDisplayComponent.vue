<template>
  <a @click="$dialog.open(DataPointDataTable, modalOptions)" v-if="hasAuxiliaryData || hasValidValue" class="link"
    >{{ contentDisplayValue }}
    <em class="pl-2 material-icons" aria-hidden="true" title=""> dataset </em>
  </a>
  <span v-else>{{ NO_DATA_PROVIDED }}</span>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import DataPointDataTable from '@/components/general/DataPointDataTable.vue';
import { NO_DATA_PROVIDED, ONLY_AUXILIARY_DATA_PROVIDED } from '@/utils/Constants';

export default defineComponent({
  name: 'DataPointDisplayComponent',
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent>,
      required: true,
    },
  },
  data() {
    return {
      DataPointDataTable,
      NO_DATA_PROVIDED: NO_DATA_PROVIDED,
    };
  },
  computed: {
    modalOptions() {
      return {
        props: {
          header: this.content.displayValue.fieldLabel,
          modal: true,
          dismissableMask: true,
        },
        data: {
          dataPointDisplay: this.convertedValueForModal,
        },
      };
    },
    hasAuxiliaryData() {
      return (
        this.content.displayValue?.dataSource?.fileName ??
        this.content.displayValue?.dataSource?.fileReference ??
        this.content.displayValue?.comment
      );
    },
    hasValidValue() {
      return this.content?.displayValue?.value && this.content?.displayValue?.value !== NO_DATA_PROVIDED;
    },
    contentDisplayValue() {
      return this.hasAuxiliaryData && !this.hasValidValue
        ? this.qualityDisplayValue
        : this.content.displayValue?.value || NO_DATA_PROVIDED;
    },
    qualityDisplayValue() {
      return this.content.displayValue.quality == '' ? ONLY_AUXILIARY_DATA_PROVIDED : this.content.displayValue.quality;
    },
    convertedValueForModal() {
      const content = this.content.displayValue;
      return {
        value: content.value,
        quality: content.quality,
        dataSource: content.dataSource,
        comment: content.comment,
      };
    },
  },
});
</script>
