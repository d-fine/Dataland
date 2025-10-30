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
      <Select
          v-model="selectedDocument"
          :options="availableDocuments"
          optionLabel="label"
          optionValue="value"
          placeholder="Select Data Source"
          fluid
      />
      <div
          v-if="selectedDocumentMeta"
          class="dataland-info-text small"
          style="background-color: var(--p-blue-50); margin: var(--spacing-xs);"
      >
        <div><strong>Name:</strong> {{ selectedDocumentMeta.documentName }}</div>
        <div><strong>Category:</strong> {{ selectedDocumentMeta.documentCategory ?? '–' }}</div>
        <div><strong>Publication Date:</strong> {{ selectedDocumentMeta.publicationDate ?? '–' }}</div>
        <div><strong>Reporting Period:</strong> {{ selectedDocumentMeta.reportingPeriod ?? '–' }}</div>
       </div>
      <h4>Comment</h4>
      <InputText :placeholder="dataPointProperties.comment ?? 'Insert comment'" fluid/>
      <PrimeButton
          label="SAVE CHANGES"
          icon="pi pi-save"
          style="margin-top: var(--spacing-md)"
      />
    </Dialog>
  </div>
</template>


<script setup lang="ts">
import {computed, inject, ref, watch} from 'vue';
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
import InputText from 'primevue/inputtext'
import {ApiClientProvider} from "@/services/ApiClients.ts";
import {assertDefined} from "@/utils/TypeScriptUtils.ts";
import type Keycloak from "keycloak-js";
import {DocumentMetaInfoResponse} from "@clients/documentmanager";

const availableDocuments = ref<{ label: string; value: string }[]>([]);
const selectedDocument = ref<string | null>(null);

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());


const editModeIsOn = inject('editModeIsOn')
const companyId = inject<string>('companyID')
const showEditModal = ref(false)
const props = defineProps<{
  content: MLDTDisplayObject<MLDTDisplayComponentName.DataPointWrapperDisplayComponent>;
  metaInfo: DataMetaInformation;
}>();

const allDocuments = ref<DocumentMetaInfoResponse[]>([]);
const selectedDocumentMeta = computed(() =>
    allDocuments.value.find((doc) => doc.documentId === selectedDocument.value)
);

watch(showEditModal, async (isVisible) => {
  if (isVisible) {
    await updateDocumentsList();
  }
});

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

async function updateDocumentsList(): Promise<void> {
  try {
    const documentControllerApi = apiClientProvider.apiClients.documentController;
    const response = await documentControllerApi.searchForDocumentMetaInformation(companyId);
    allDocuments.value = response.data;

    availableDocuments.value = allDocuments.value
        .filter((doc) => doc.documentName && doc.documentId)
        .map((doc) => ({
          label: doc.documentName!,
          value: doc.documentId,
        }));
  } catch (error) {
    console.error('Error fetching documents:', error);
    allDocuments.value = [];
    availableDocuments.value = [];
  }
}


</script>
