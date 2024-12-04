<template>
  <PrimeDialog
    :dismissableMask="true"
    :modal="true"
    v-if="isDownloadModalOpen"
    :visible="isDownloadModalOpen"
    :closable="true"
    style="text-align: center; width: 20%"
    :show-header="true"
  >
    <template #header>
      <span style="font-weight: bold; margin-right: auto">Download dataset</span>
    </template>
    <FormKit type="form" :actions="false" class="formkit-wrapper">
      <label for="Download">
        <b style="margin-bottom: 8px; font-weight: normal">Reporting year</b>
      </label>
      <FormKit
        v-model="selectedReportingYear"
        type="select"
        name="reportingYearSelector"
        data-test="reportingYearSelector"
        :options="['2022', '2023', '2024']"
        placeholder="Select a reporting year"
      />
      <label for="Download">
        <b style="margin-bottom: 8px; font-weight: normal">File format</b>
      </label>
      <FormKit
        v-model="selectedFormat"
        type="select"
        name="formatSelector"
        data-test="formatSelector"
        :options="['json', 'csv']"
        placeholder="Select a file format"
      />
    </FormKit>
    <div>
      <PrimeButton
        data-test="downloadDataButton"
        @click="downloadData()"
        style="width: 100%; justify-content: center"
        :disabled="selectedFormat === '' || selectedReportingYear === ''"
      >
        <span class="d-letters" style="text-align: center" data-test="downloadButton"> DOWNLOAD </span>
      </PrimeButton>
    </div>
  </PrimeDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';

export default defineComponent({
  components: { PrimeDialog, PrimeButton },
  name: 'DownloadDataSetModal',
  props: {
    isDownloadModalOpen: {
      type: Boolean,
      required: true,
    },
    handleClose: {
      type: Function,
      required: true,
    },
    handleSave: {
      type: Function,
      required: true,
    },
  },
  data() {
    return {
      selectedReportingYear: '',
      selectedFormat: '',
      isSavingFile: false,
    };
  },
  methods: {
    /**
     * Downloads the selected dataset
     */
    downloadData() {
      this.isSavingFile = true;
      this.handleSave(this.selectedReportingYear, this.selectedFormat);

      this.closeModal();
      this.isSavingFile = false;
    },
    /**
     * Closes the download modal
     */
    closeModal() {
      this.handleClose();
    },
  },
});
</script>

<style></style>
