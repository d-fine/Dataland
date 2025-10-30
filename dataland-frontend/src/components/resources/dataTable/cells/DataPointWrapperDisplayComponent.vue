<template>
  <div class="flex">
    <PrimeButton
        variant="text"
        v-if="isAnyDataPointPropertyAvailableThatIsWorthShowingInModal"
        @click="$dialog.open(DataPointDataTable, modalOptions)"
    >
      <slot></slot>
      <em v-if="!editModeIsOn" class="pi pi-eye" style="padding-left: var(--spacing-md)"> </em>
    </PrimeButton>
    <div v-else-if="dataPointProperties.value">
      <slot>{{ dataPointProperties.value }}</slot>
    </div>
    <div v-else>
      <slot></slot>
    </div>
    <PrimeButton
        v-if="editModeIsOn"
        icon="pi pi-pencil"
        variant="text"
        @click="showEditModal=true"
    />
    <Dialog
        v-model:visible="showEditModal"
        header="Edit Data Point"
        :modal="true"
    ><h4>Value</h4>
      <InputNumber :placeholder="dataPointProperties.value ?? 'Insert value'" fluid/>
      <h4>Quality</h4>
      <Select :placeholder="dataPointProperties.quality ?? 'Select Quality'" fluid/>
      <h4>Data Source</h4>
      <Select :placeholder="dataPointProperties.dataSource?.fileName ?? 'Select Datasource'" fluid/>

    </Dialog>
  </div>
</template>


<script setup lang="ts">
import {computed, inject, ref} from 'vue';
import {
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import DataPointDataTable from '@/components/general/DataPointDataTable.vue';
import {type DataMetaInformation, type ExtendedDocumentReference} from '@clients/backend';
import {isDatapointCommentConsideredMissing} from '@/components/resources/dataTable/conversion/DataPoints';
import PrimeButton from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'

const editModeIsOn = inject('editModeIsOn')
const showEditModal = ref(false)
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
      !isDatapointCommentConsideredMissing(dataPointProperties.value) ||
      quality != undefined ||
      dataSource != undefined
  );
});
</script>
