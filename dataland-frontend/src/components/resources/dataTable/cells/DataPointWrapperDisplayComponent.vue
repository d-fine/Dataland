<template>
  <div class="flex">
    <PrimeButton
      variant="link"
      v-if="isAnyDataPointPropertyAvailableThatIsWorthShowingInModal"
      @click="$dialog.open(DataPointDataTable, modalOptions)"
    >
      <slot></slot>
    </PrimeButton>
    <div v-else-if="dataPointProperties.value">
      <slot>{{ dataPointProperties.value }}</slot>
    </div>
    <div v-else>
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import DataPointDataTable from '@/components/general/DataPointDataTable.vue';
import { type DataMetaInformation, type ExtendedDocumentReference } from '@clients/backend';
import { isDatapointCommentConsideredMissing } from '@/components/resources/dataTable/conversion/DataPoints';
import PrimeButton from 'primevue/button';

const props = defineProps<{
  content: MLDTDisplayObject<MLDTDisplayComponentName.DataPointWrapperDisplayComponent>;
  metaInfo: DataMetaInformation;
}>();

const modalOptions = computed(() => {
  return {
    props: {
      header: props.content.displayValue.fieldLabel,
      modal: true,
      dismissableMask: true,
      style: {
        maxWidth: '80vw',
      },
    },
    data: {
      dataPointDisplay: dataPointProperties.value,
      dataId: props.metaInfo.dataId,
      dataType: props.metaInfo.dataType,
    },
  };
});

const dataPointProperties = computed(() => {
  const content = props.content.displayValue;
  let valueOption = undefined;
  if (content.innerContents.displayComponentName == MLDTDisplayComponentName.StringDisplayComponent) {
    valueOption = content.innerContents.displayValue;
  }
  return {
    value: valueOption,
    quality: content.quality,
    dataSource: content.dataSource,
    comment: content.comment,
  };
});

const isAnyDataPointPropertyAvailableThatIsWorthShowingInModal = computed(() => {
  const dataSource = dataPointProperties.value.dataSource as ExtendedDocumentReference | undefined | null;
  const quality = dataPointProperties.value.quality;

  return (
    !isDatapointCommentConsideredMissing(dataPointProperties.value) || quality != undefined || dataSource != undefined
  );
});
</script>
