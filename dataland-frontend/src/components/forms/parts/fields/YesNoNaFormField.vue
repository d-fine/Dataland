<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit v-if="certificateRequiredIfYes" v-model="baseDataPointYesNo" type="group" :name="name">
    <RadioButtonsFormField
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :label="label"
      :options="yesNoNaOptions"
    />
    <UploadDocumentsForm
        v-show="baseDataPointYesNoNa.value === 'Yes'"
        @documentsChanged="handleDocumentUpdatedEvent"
        ref="uploadDocumentsForm"
        :name="name"
        :more-than-one-document-allowed="false"
        :file-names-for-prefill="fileNamesForPrefill"
    />
    <FormKit v-if="baseDataPointYesNoNa.value === 'Yes'" type="group" name="dataSource">
      <FormKit type="hidden" name="name" v-model="documentName" />
      <FormKit type="text" name="reference" v-model="documentReference" :outer-class="{ 'hidden-input': true }" />
    </FormKit>
    </FormKit>

    <RadioButtonsFormElement
        v-else
        :name="name"
        :validation="validation"
        :validation-label="validationLabel ?? label"
        :options="yesNoNaOptions"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import RadioButtonsFormField from "@/components/forms/parts/fields/RadioButtonsFormField.vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import {BaseDataPointYesNoNa} from "@clients/backend";
import {DocumentToUpload} from "@/utils/FileUploadUtils";

export default defineComponent({
  name: "YesNoNaFormField",
  components: { RadioButtonsFormElement, UploadFormHeader, UploadDocumentsForm },
  inheritAttrs: false,
  props: { ...YesNoFormFieldProps, dataTest: String },
  data() {
    return {
      baseDataPointYesNoNa: {} as BaseDataPointYesNoNa,
      referencedDocument: undefined as DocumentToUpload | undefined,
      documentName: "",
      documentReference: "",
      fileNamesForPrefill: [] as string[],
      yesNoNaOptions: {
        Yes: "Yes",
        No: "No",
        NA: "N/A"
      },
      isMounted: false,
    };
  },

  emits: ["documentUpdated"],
  mounted() {
    this.updateFileUploadFiles();
    this.isMounted = true;
  },
  watch: {
    baseDataPointYesNo(newValue: BaseDataPointYesNoNa, oldValue: BaseDataPointYesNoNa) {
      if (newValue.value === "No" && oldValue.value === "Yes" && this.certificateRequiredIfYes) {
        (this.$refs.uploadDocumentsForm.removeAllDocuments as () => void)();
      }
    },
    documentName() {
      if (this.isMounted) {
        this.updateFileUploadFiles();
      }
    },
  },
  methods: {
    /**
     * Emits event that selected document changed
     * @param updatedDocuments the updated documents that are currently selected (only one in this case)
     */
    handleDocumentUpdatedEvent(updatedDocuments: DocumentToUpload[]) {
      this.referencedDocument = updatedDocuments[0];
      this.documentName = this.referencedDocument?.fileNameWithoutSuffix ?? "";
      this.documentReference = this.referencedDocument?.reference ?? "";
      this.$emit("documentUpdated", this.name, this.referencedDocument);
    },

    /**
     * updates the files in the fileUpload file list to represent that a file was already uploaded in a previous upload
     * of the given dataset (in the case of editing a dataset)
     */
    updateFileUploadFiles() {
      if (this.documentName !== "" && this.referencedDocument === undefined) {
        this.fileNamesForPrefill = [this.documentName];
      }
    },
  },
});
</script>
