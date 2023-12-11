<template>
  <div class="grid">
    <p>!!-- {{ currentReportValue }}</p>
    <FormKit type="group" :name="name" v-model="dataPoint">
      <div class="col-12">
        <slot />
        <div class="grid align-content-end">
          <div class="col-8">
            <UploadFormHeader
              :label="`${label} Report`"
              description="Select a report as a reference for this data point."
            />
            <FormKit
              type="select"
              name="fileName"
              v-model="currentReportValue"
              placeholder="Select a report"
              :options="[noReportLabel, ...reportsName]"
              :ignore="true"
            />
          </div>
          <div class="col-4">
            <UploadFormHeader :label="'Page'" :description="'Page where information was found'" />
            <FormKit
              outer-class="w-100"
              type="number"
              name="page"
              placeholder="Page"
              v-model="pageForFileReference"
              validation-label="Page"
              step="1"
              min="0"
              validation="min:0"
              :ignore="true"
            />
          </div>

          <FormKit
            v-if="
              (currentReportValue && currentReportValue !== noReportLabel) ||
              (dataPoint?.dataSource?.fileName && dataPoint?.dataSource?.fileName !== noReportLabel)
            "
            type="group"
            name="dataSource"
          >
            <FormKit type="text" name="fileName" v-model="currentReportValue" :outer-class="{ 'hidden-input': true }" />
            <FormKit
              type="text"
              name="fileReference"
              :modelValue="fileReferenceAccordingToName"
              :outer-class="{ 'hidden-input': true }"
            />
            <FormKit type="number" name="page" v-model="pageForFileReference" :outer-class="{ 'hidden-input': true }" />
          </FormKit>
        </div>

        <!-- Data quality -->
        <div class="md:col-8 col-12 p-0 mb-4" data-test="dataQuality">
          <UploadFormHeader
            :label="`${label} Quality`"
            description="The level of confidence associated to the value."
            :is-required="isDataQualityRequired"
          />
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
        <div class="form-field">
          <FormKit
            type="textarea"
            name="comment"
            placeholder="(Optional) Add comment that might help Quality Assurance to approve the datapoint. "
          />
        </div>
      </div>
    </FormKit>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import { QualityOptions } from "@clients/backend";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { type ObjectType } from "@/utils/UpdateObjectUtils";
import { getFileName, getFileReferenceByFileName } from "@/utils/FileUploadUtils";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "ExtendedDataPointFormField",
  components: { UploadFormHeader, FormKit },
  inject: {
    injectReportsNameAndReferences: {
      from: "namesAndReferencesOfAllCompanyReportsForTheDataset",
      default: {} as ObjectType,
    },
  },
  computed: {
    isDataValueProvided(): boolean {
      return (assertDefined(this.checkValueValidity) as (dataPoint: unknown) => boolean)(this.dataPoint);
    },
    isDataQualityRequired(): boolean {
      return this.isDataValueProvided;
    },
    computeQualityOption(): object {
      if (!this.isDataValueProvided) {
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
  data() {
    return {
      qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
        label: qualityOption,
        value: qualityOption,
      })),
      qualityValue: "NA",
      currentReportValue: this.noReportLabel,
      dataPoint: {} as unknown,
      noReportLabel: "None...",
      pageForFileReference: null,
    };
  },

  props: {
    ...BaseFormFieldProps,
    checkValueValidity: {
      type: Function as unknown as () => (dataPoint: unknown) => boolean,
      default: (): boolean => false,
    },
  },
  watch: {
    isDataValueProvided(isDataValueProvided) {
      this.handleBlurValue(isDataValueProvided);
    },
  },
  methods: {
    /**
     * Handle blur event on value input.
     * @param isDataValueProvided boolean which gives information whether data is provided or not
     */
    handleBlurValue(isDataValueProvided) {
      if (isDataValueProvided === false) {
        1;
        this.qualityValue = QualityOptions.Na;
      } else if (this.qualityValue === QualityOptions.Na) {
        this.qualityValue = "";
      }
    },
  },
});
</script>
