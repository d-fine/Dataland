<template>
  <FormKit type="group" :name="name">
    <div class="mb-3">
      <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
      <div class="next-to-each-other">
        <FormKit
          type="text"
          name="value"
          v-model="currentValue"
          :validation-label="validationLabel ?? label"
          :validation="`number|${validation}`"
          :placeholder=" unit ? `Value in ${unit}` : 'Value' "
          outer-class="short"
          @blur="handleBlurValue"
        />
        <div v-if="unit" class="form-field-label pb-3">
          <FormKit type="hidden" name="currency" />
          <span>{{ unit }}</span>
        </div>
        <FormKit
          v-else-if="options"
          type="select"
          name="currency"
          placeholder="Currency"
          :options="options"
          outer-class="short"
          data-test="datapoint-currency"
        />
      </div>
    </div>
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
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { type ObjectType } from "@/utils/UpdateObjectUtils";

export default defineComponent({
  name: "DataPointFormField",
  components: { UploadFormHeader, FormKit },
  inject: {
    injectReportsNameAndReferences: {
      from: "namesAndReferencesOfAllCompanyReportsForTheDataset",
      default: {} as ObjectType,
    },
  },
  computed: {
    isDataQualityRequired(): boolean {
      return this.currentValue !== "";
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
  data() {
    return {
      qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
        label: qualityOption,
        value: qualityOption,
      })),
      qualityValue: "NA",
      currentValue: "",
      currentReportValue: "",
    };
  },
  props: {
    ...YesNoFormFieldProps,
    options: {
      type: Array,
    },
  },
  methods: {
    /**
     * Handle blur event on value input.
     */
    handleBlurValue() {
      if (this.currentValue === "") {
        this.qualityValue = "NA";
      } else if (this.currentValue !== "" && this.qualityValue == "NA") {
        this.qualityValue = "";
      }
    },
  },
});
</script>
