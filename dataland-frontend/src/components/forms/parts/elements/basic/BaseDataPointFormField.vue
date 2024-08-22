<template>
  <div class="form-field">
    <div class="">
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
      <FormKit
        type="checkbox"
        name="name"
        v-model="checkboxValue"
        :options="options"
        :outer-class="{
          'yes-no-radio': true,
        }"
        :inner-class="{
          'formkit-inner': false,
        }"
        :input-class="{
          'formkit-input': false,
          'p-radiobutton': true,
        }"
        :ignore="true"
        :plugins="[disabledOnMoreThanOne]"
        @input="updateCurrentValue($event)"
      />
    </div>

    <div v-if="showDataPointFields">
      <FormKit v-model="baseDataPoint" type="group" :name="name">
        <FormKit
          type="text"
          name="value"
          v-model="currentValue"
          :validation="validation"
          :validation-label="validationLabel"
          :outer-class="{ 'hidden-input': true, 'formkit-outer': false }"
        />
        <div class="col-12" v-if="baseDataPoint.value === 'Yes'">
          <UploadDocumentsForm
            @updatedDocumentsSelectedForUpload="handleDocumentUpdatedEvent"
            ref="uploadDocumentsForm"
            :name="name"
            :more-than-one-document-allowed="false"
            :file-names-for-prefill="fileNamesForPrefill"
          />
        </div>
        <FormKit v-if="isValidFileName(isMounted, documentName)" type="group" name="dataSource">
          <FormKit type="hidden" name="fileName" v-model="documentName" />
          <FormKit type="hidden" name="fileReference" v-model="documentReference" />
        </FormKit>
      </FormKit>
    </div>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import UploadDocumentsForm from '@/components/forms/parts/elements/basic/UploadDocumentsForm.vue';
import { type DocumentToUpload } from '@/utils/FileUploadUtils';
import { type BaseDataPoint } from '@/utils/DataPoint';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { disabledOnMoreThanOne } from '@/utils/FormKitPlugins';
import { isValidFileName } from '@/utils/DataSource';

export default defineComponent({
  name: 'BaseDataPointFormField',
  components: { UploadFormHeader, UploadDocumentsForm },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    options: {
      type: Object,
      require: true,
    },
  },
  inject: {
    injectlistOfFilledKpis: {
      from: 'listOfFilledKpis',
      default: [] as Array<string>,
    },
  },
  data() {
    return {
      dataPointIsAvailable: (this.injectlistOfFilledKpis as unknown as Array<string>).includes(this.name as string),
      baseDataPoint: {} as BaseDataPoint<unknown>,
      referencedDocument: undefined as DocumentToUpload | undefined,
      documentName: '',
      documentReference: '',
      fileNamesForPrefill: [] as string[],
      isMounted: false,
      isValidFileName: isValidFileName,
      currentValue: null,
      checkboxValue: [] as Array<string>,
    };
  },
  computed: {
    showDataPointFields(): boolean {
      return this.dataPointIsAvailable;
    },
  },
  emits: ['fieldSpecificDocumentsUpdated'],
  mounted() {
    setTimeout(() => {
      this.isMounted = true;
      this.updateFileUploadFiles();
    });
  },
  watch: {
    documentName() {
      if (this.isMounted) {
        this.updateFileUploadFiles();
      }
    },
    currentValue(newVal: string) {
      this.setCheckboxValue(newVal);
    },
  },
  methods: {
    disabledOnMoreThanOne,
    /**
     * A function that rewrite value to select the appropriate checkbox
     * @param newCheckboxValue value after changing value that must be reflected in checkboxes
     */
    setCheckboxValue(newCheckboxValue: string) {
      if (newCheckboxValue && newCheckboxValue !== '') {
        this.checkboxValue = [newCheckboxValue];
      }
    },

    /**
     * Emits event that selected document changed
     * @param updatedDocuments the updated documents that are currently selected (only one in this case)
     */
    handleDocumentUpdatedEvent(updatedDocuments: DocumentToUpload[]) {
      this.referencedDocument = updatedDocuments[0];
      this.documentName = this.referencedDocument?.fileNameWithoutSuffix ?? '';
      this.documentReference = this.referencedDocument?.fileReference ?? '';
      this.$emit('fieldSpecificDocumentsUpdated', this.referencedDocument);
    },

    /**
     * updates the files in the fileUpload file list to represent that a file was already uploaded in a previous upload
     * of the given dataset (in the case of editing a dataset)
     */
    updateFileUploadFiles() {
      if (this.documentName !== '' && this.referencedDocument === undefined) {
        this.fileNamesForPrefill = [this.documentName];
      }
    },
    /**
     * updateCurrentValue
     * @param checkboxValue checkboxValue
     */
    updateCurrentValue(checkboxValue: [string]) {
      if (checkboxValue[0]) {
        this.dataPointIsAvailable = true;
        this.baseDataPoint.value = checkboxValue[0].toString();
      } else {
        this.dataPointIsAvailable = false;
      }
    },
  },
});
</script>
