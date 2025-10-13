<template>
  <div class="p-datatable p-component">
    <Button label="EDIT MODE" class="p-button-outlined" @click="toggleEditMode" />
    <div class="p-datatable-wrapper overflow-auto">
      <table class="p-datatable-table" :aria-label="ariaLabel">
        <thead class="p-datatable-thead">
          <tr class="border-bottom-table">
            <th class="horizontal-headers-size">
              <div class="p-column-header-content">
                <span class="p-column-title">KPIs</span>
              </div>
            </th>
            <th
              v-for="(singleDataAndMetaInfo, idx) in dataAndMetaInfo"
              :key="idx"
              class="horizontal-headers-size"
              :data-dataset-index="idx"
            >
              <div class="p-column-header-content">
                <span class="p-column-title" style="display: flex; align-items: center">
                  {{ singleDataAndMetaInfo.metaInfo.reportingPeriod }}
                </span>
              </div>
            </th>
          </tr>
        </thead>
        <tbody class="p-datatable-tbody">
          <tr
            v-for="(singleDataAndMetaInfo, idx) in dataAndMetaInfo"
            :key="idx"
          >
            <td class="headers-bg pl-4 vertical-align-top header-column-width">
              <span class="table-left-label">{{ singleDataAndMetaInfo.metaInfo.kpi }}</span>
              <Button v-if="isEditMode" icon="pi pi-pencil" class="p-button-text" @click="openEditModal(singleDataAndMetaInfo)" />
            </td>
            <td v-for="(data, dataIdx) in singleDataAndMetaInfo.data" :key="dataIdx" class="vertical-align-top">
              <span>{{ data.value }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <Dialog v-if="isModalOpen" v-model:visible="isModalOpen" header="Edit Data Point" :modal="true" :closable="false">
      <form @submit.prevent="saveDataPoint()">
        <div class="form-group">
          <label for="value">Value</label>
          <InputText
            id="value"
            v-model="dummyValue"
            required
          />
        </div>
        <div class="form-group">
          <label for="document">Document</label>
          <Dropdown
            id="document"
            v-model="dummyDocument"
            :options="availableDocuments"
            optionLabel="name"
            optionValue="id"
            required
          />
        </div>
        <div class="modal-actions">
          <Button label="Cancel" class="p-button-text" @click="closeEditModal" />
          <Button label="Save" type="submit" class="p-button-primary" />
        </div>
      </form>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, defineProps, PropType, UnwrapRef, Ref} from 'vue';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';

const props = defineProps({
  dataAndMetaInfo: {
    type: Array as PropType<Array<{ metaInfo: { kpi: string; reportingPeriod: string }; data: Array<{ value: string }> }>>,
    required: true,
  },
  ariaLabel: {
    type: String,
    required: true,
  },
});

const isEditMode = ref(false);
const isModalOpen = ref(false);
const selectedDataPoint = ref<{ metaInfo: { kpi: string; reportingPeriod: string }; data: Array<{ value: string }>; document?: string } | null>(null);
const availableDocuments = ref<Array<{ id: string; name: string }>>([
  { id: 'doc1', name: 'Document 1' },
  { id: 'doc2', name: 'Document 2' },
]);
const dummyValue = ref('');
const dummyDocument = ref('');

function toggleEditMode() {
  isEditMode.value = !isEditMode.value;
}

function openEditModal(dataPoint: { metaInfo: { kpi: string; reportingPeriod: string }; data: Array<{ value: string }>; document?: string }) {
  selectedDataPoint.value = { ...dataPoint };
  isModalOpen.value = true;
}

function closeEditModal() {
  isModalOpen.value = false;
  selectedDataPoint.value = null;
}

function saveDataPoint() {
  if (selectedDataPoint.value) {
    console.log('Saving data point:', selectedDataPoint.value);
    closeEditModal();
  }
}
</script>

<style scoped>
.p-datatable-table {
  border-spacing: 0;
  border-collapse: collapse;
}

.border-bottom-table {
  border-bottom: 1px solid var(--table-border);
}

.headers-bg {
  background-color: #f9f9f9;
}

.form-group {
  margin-bottom: 1rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
