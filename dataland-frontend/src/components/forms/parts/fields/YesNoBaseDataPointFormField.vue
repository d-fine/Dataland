<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit v-model="baseDataPointYesNo" type="group" :name="name">
      <RadioButtonsFormElement
        name="value"
        :validation="validation"
        :validation-label="validationLabel ?? label"
        :options="yesNoOptions"
        :data-test="dataTest"
      />
      <UploadDocumentsForm
        v-show="baseDataPointYesNo.value === 'Yes'"
        @updatedDocumentsSelectedForUpload="handleDocumentUpdatedEvent"
        ref="uploadDocumentsForm"
        :name="name"
        :more-than-one-document-allowed="false"
        :file-names-for-prefill="fileNamesForPrefill"
      />
      <FormKit v-if="baseDataPointYesNo.value === 'Yes'" type="group" name="dataSource">
        <FormKit type="hidden" name="fileName" v-model="documentName" />
        <FormKit type="hidden" name="fileReference" v-model="documentReference" />
      </FormKit>
    </FormKit>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import { type DocumentToUpload, getFileName } from "@/utils/FileUploadUtils";
import { type BaseDataPointYesNo } from "@clients/backend";
import { type ObjectType } from "@/utils/UpdateObjectUtils";

export default defineComponent({
  name: "YesNoBaseDataPointFormField",
  components: { RadioButtonsFormElement, UploadFormHeader, UploadDocumentsForm },
  inheritAttrs: false,
  inject: {
    injectReportsNameAndReferences: {
      from: "namesAndReferencesOfAllCompanyReportsForTheDataset",
      default: {} as ObjectType,
    },
  },
  props: {
    ...YesNoFormFieldProps,
    dataTest: String,
  },

  data() {
    return {
      baseDataPointYesNo: {} as BaseDataPointYesNo,
      referencedDocument: undefined as DocumentToUpload | undefined,
      documentName: "",
      documentReference: "",
      fileNamesForPrefill: [] as string[],
      yesNoOptions: {
        Yes: "Yes",
        No: "No",
      },
      isMounted: false,
    };
  },
  computed: {
    reportsName(): string[] {
      return getFileName(this.injectReportsNameAndReferences);
    },
  },
  emits: ["reportsUpdated"],
  mounted() {
    this.updateFileUploadFiles();
    this.isMounted = true;
  },
  watch: {
    baseDataPointYesNo(newValue: BaseDataPointYesNo, oldValue: BaseDataPointYesNo) {
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
      this.documentReference = this.referencedDocument?.fileReference ?? "";
      this.$emit("reportsUpdated", this.documentName, this.referencedDocument);
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
