<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit v-if="certificateRequiredIfYes" v-model="baseDataPointYesNo" type="group" :name="name">
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
    <div v-else-if="evidenceDesired">
      <FormKit v-model="baseDataPointYesNo" type="group" :name="name">
        <div class="mb-3">
          <RadioButtonsFormElement
            name="value"
            v-model="currentValue"
            :validation="validation"
            :validation-label="validationLabel ?? label"
            :options="yesNoOptions"
            :data-test="dataTest"
            @blur="handleBlurValue"
          />
        </div>
        <div>
          <FormKit type="group" name="dataSource">
            <div class="next-to-each-other">
              <div class="flex-1">
                <UploadFormHeader :label="`${label} Report`" :description="'Upload Report'" />
                <FormKit
                  type="select"
                  name="fileName"
                  v-model="currentReportValue"
                  placeholder="Select a report"
                  :options="['None...', ...reportsName]"
                />
                <FormKit type="hidden" name="fileReference" :modelValue="fileReferenceAccordingToName" />
              </div>
              <div>
                <UploadFormHeader :label="'Page'" :description="'Page where information was found'" />
                <FormKit
                  outer-class="w-100"
                  type="number"
                  name="page"
                  placeholder="Page"
                  validation-label="Page"
                  step="1"
                  min="0"
                  validation="min:0"
                />
              </div>
            </div>
          </FormKit>
        </div>

        <!-- Data quality -->
        <div class="mb-4">
          <UploadFormHeader
            :label="`${label} Quality`"
            description="The level of confidence associated to the value."
            :is-required="isDataQualityRequired"
          />
          <div class="md:col-6 col-12 p-0">
            <FormKit
              type="select"
              v-model="qualityValue"
              name="quality"
              :validation="isDataQualityRequired ? 'required' : ''"
              validation-label="Data quality"
              placeholder="Data quality"
              :options="computeQualityOption"
            />
          </div>
        </div>
        <div class="form-field">
          <FormKit
            type="textarea"
            name="comment"
            placeholder="(Optional) Add comment that might help Quality Assurance to approve the datapoint. "
          />
        </div>
      </FormKit>
    </div>
    <RadioButtonsFormElement
      v-else
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :options="yesNoOptions"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import { type DocumentToUpload } from "@/utils/FileUploadUtils";
import { type BaseDataPointYesNo, QualityOptions } from "@clients/backend";
import { type ObjectType } from "@/utils/UpdateObjectUtils";

export default defineComponent({
  name: "YesNoFormField",
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
      qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
        label: qualityOption,
        value: qualityOption,
      })),
      currentValue: undefined,
      currentReportValue: "",
      qualityValue: "NA",
    };
  },
  computed: {
    isDataQualityRequired(): boolean {
      return this.currentValue ?? false;
    },
    computeQualityOption(): object {
      if (this.currentValue == "") {
        return this.qualityOptions;
      } else {
        return this.qualityOptions.filter((qualityOption) => qualityOption.value !== QualityOptions.Na);
      }
    },
    reportsName(): string[] {
      if (this.injectReportsNameAndReferences) {
        return Object.keys(this.injectReportsNameAndReferences);
      } else {
        return [];
      }
    },
    fileReferenceAccordingToName(): string {
      if (this.injectReportsNameAndReferences) {
        return this.injectReportsNameAndReferences[
          this.currentReportValue as keyof typeof this.injectReportsNameAndReferences
        ];
      } else {
        return "";
      }
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
    /**
     * Handle blur event on value input.
     */
    handleBlurValue() {
      if (this.currentValue === undefined) {
        this.qualityValue = "NA";
      } else if (this.currentValue !== "" && this.qualityValue == "NA") {
        this.qualityValue = "";
      }
    },
  },
});
</script>
