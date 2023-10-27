<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
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
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { getFileName, getFileReferenceByFileName } from "@/utils/FileUploadUtils";
import { type BaseDataPointYesNo, QualityOptions } from "@clients/backend";
import { type ObjectType } from "@/utils/UpdateObjectUtils";

export default defineComponent({
  name: "YesNoFormField",
  components: { RadioButtonsFormElement, UploadFormHeader },
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
      yesNoOptions: {
        Yes: "Yes",
        No: "No",
      },
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
      return !!this.currentValue;
    },
    computeQualityOption(): object {
      if (this.currentValue == "") {
        return this.qualityOptions;
      } else {
        return this.qualityOptions.filter((qualityOption) => qualityOption.value !== QualityOptions.Na);
      }
    },
    reportsName(): string[] {
      return getFileName(this.injectReportsNameAndReferences);
    },
    fileReferenceAccordingToName(): string {
      return getFileReferenceByFileName(this.currentReportValue, this.injectReportsNameAndReferences);
    },
  },
  emits: ["reportsUpdated"],
  methods: {
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
