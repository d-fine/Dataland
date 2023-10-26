<template>
  <FormKit type="group" :name="name">
    <slot />
    <div>
      <FormKit type="group" name="dataSource">
        <div class="next-to-each-other">
          <div class="flex-1">
            <UploadFormHeader
              :label="`${label} Report`"
              :description="'Select a report as a reference for this data point.'"
            />
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
    <div class="mb-4" data-test="dataQuality">
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
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import { QualityOptions } from "@clients/backend";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { type ObjectType } from "@/utils/UpdateObjectUtils";
import { getFileName, getFileReferenceByFileName } from "@/utils/FileUploadUtils";

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
      currentReportValue: "",
    };
  },
  props: {
    ...BaseFormFieldProps,
    isDataValueProvided: {
      type: Boolean,
      required: true,
    },
  },
  methods: {
    setQuality(qualityOption: QualityOptions | undefined) {
      this.qualityValue = qualityOption ?? "";
    },
    isQualityNa(): boolean {
      return this.qualityValue === QualityOptions.Na;
    },
  },
});
</script>
