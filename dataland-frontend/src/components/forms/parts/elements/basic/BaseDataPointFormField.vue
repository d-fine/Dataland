<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <div class="yes-no-checkboxes">
      <div v-for="(labelText, value) in options" :key="value" class="yes-no-option">
        <Checkbox v-model="checkboxValue" :inputId="`yes-no-${value}`" :value="value" @change="updateYesNoValue()" />
        <label :for="`yes-no-${value}`">{{ labelText }}</label>
      </div>
    </div>

    <div v-if="showDataPointFields">
      <FormKit v-model="baseDataPoint" type="group" :name="name">
        <FormKit
          type="text"
          name="value"
          v-model="yesNoValue"
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
import Checkbox from 'primevue/checkbox';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import UploadDocumentsForm from '@/components/forms/parts/elements/basic/UploadDocumentsForm.vue';
import { type DocumentToUpload } from '@/utils/FileUploadUtils';
import { type BaseDataPoint } from '@/utils/DataPoint';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { isValidFileName } from '@/utils/DataSource';

export default defineComponent({
  name: 'BaseDataPointFormField',
  components: { UploadFormHeader, UploadDocumentsForm, Checkbox },
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
      yesNoValue: undefined as string | undefined,
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

    checkboxValue(newArr: string[]) {
      if (newArr.length > 1) {
        const last = newArr.at(-1);
        this.checkboxValue = [last];
        this.yesNoValue = last;
      } else if (newArr.length === 1) {
        const [only] = newArr;
        if (this.yesNoValue !== only) {
          this.yesNoValue = only;
        }
      } else {
        this.yesNoValue = undefined;
      }
    },
  },
  methods: {
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
     * updateCurrentValue
     */
    updateYesNoValue() {
      if (this.checkboxValue.length) {
        this.dataPointIsAvailable = true;
      } else {
        this.dataPointIsAvailable = false;
        this.yesNoValue = undefined;
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
  },
});
</script>
<style scoped>
.yes-no-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.yes-no-checkboxes {
  display: flex; /* lay children out in a row */
  gap: 7rem; /* space between each checkbox+label */
  align-items: center; /* vertical align if labels differ in height */
}

.yes-no-checkboxes input[type='checkbox']:hover {
  /* pointer cursor on the box itself */
  cursor: pointer;
}

.yes-no-checkboxes label {
  /* smooth transition if you like */
  transition: background-color 0.2s ease;
}

.yes-no-checkboxes label:hover {
  /* pointer + background on hover */
  cursor: pointer;
  background-color: rgba(0, 0, 0, 0.05);
}
</style>
