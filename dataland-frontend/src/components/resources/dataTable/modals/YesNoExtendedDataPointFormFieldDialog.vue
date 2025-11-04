<template>
  <h4>Value</h4>
  <div v-for="(labelText, value) in options" :key="value" class="yes-no-option">
    <Checkbox
      class="yes-no-checkboxes"
      v-model="checkboxValue"
      :inputId="`yes-no-${value}`"
      :value="value"
      @change="updateYesNoValue()"
      :binary="true"
    />
    <label :for="`yes-no-${value}`">{{ labelText }}</label>
  </div>
  <ExtendedDataPointFormFieldDialog
    :companyID="props.companyID"
    :dataPointProperties="dataPointProperties || undefined"
    :allDocuments="allDocuments.length ? allDocuments : undefined"
    :availableDocuments="availableDocuments.length ? availableDocuments : undefined"
    v-model:selectedDocument="selectedDocument"
    v-model:insertedQuality="insertedQuality"
    v-model:insertedComment="insertedComment"
  />
  <PrimeButton label="SAVE CHANGES" icon="pi pi-save" style="margin-top: var(--spacing-md)" />
</template>

<script setup lang="ts">
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import { ref } from 'vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import PrimeButton from 'primevue/button';
import Checkbox from 'primevue/checkbox';

const dataPointProperties = ref<{
  //TODO: Remove eslint-disable-next-line
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  value: any;
  quality: string | undefined;
  //TODO: Remove eslint-disable-next-line
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  dataSource: any;
  comment: string | undefined;
} | null>(null);

const allDocuments = ref<DocumentMetaInfoResponse[]>([]);
const availableDocuments = ref<{ label: string; value: string }[]>([]);

const insertedQuality = ref<string | undefined>(undefined);
const insertedComment = ref<string | undefined>(undefined);
const selectedDocument = ref<string | undefined>(undefined);
const checkboxValue = [] as Array<string>;

const props = defineProps<{ companyID: string }>();

const options = {
  yes: 'Yes',
  no: 'No',
};

/**
 * Updates a value to represent a "Yes" or "No" state.
 */
function updateYesNoValue(): void {
  // TODO: Implement logic for updating yes/no value
}
</script>

<style scoped>
.yes-no-checkboxes {
  gap: 7rem;
  align-items: center;
}
</style>
