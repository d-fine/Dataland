<template>
  <template v-if="evidenceDesired">
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
            placeholder="Value"
            outer-class="short"
          />
          <div v-if="unit" class="form-field-label pb-3">
            <FormKit type="hidden" name="unit" :modelValue="unit" />
            <span>in {{ unit }}</span>
          </div>
          <FormKit
            v-else-if="options"
            type="select"
            name="unit"
            placeholder="Currency"
            :options="options"
            outer-class="short"
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
                name="report"
                placeholder="Select a report"
                :options="['None...', ...injectReportsName]"
                :plugins="[selectNothingIfNotExistsFormKitPlugin]"
              />
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
            :modelValue="!isDataQualityRequired ? 'NA' : ''"
            name="quality"
            :validation="isDataQualityRequired ? 'required' : ''"
            validation-label="Data quality"
            placeholder="Data quality"
            :options="qualityOptions"
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

  <template v-else>
    <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
    <FormKit
      type="text"
      :name="name"
      v-model="currentValue"
      :validation-label="validationLabel ?? label"
      :validation="`number|${validation}`"
      :placeholder="placeholder"
      :inner-class="innerClass"
    />
  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import { QualityOptions } from "@clients/backend";
import { selectNothingIfNotExistsFormKitPlugin } from "@/utils/FormKitPlugins";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "DataPointFormField",
  components: { UploadFormHeader, FormKit },
  inject: {
    injectReportsName: {
      from: "namesOfAllCompanyReportsForTheDataset",
      default: [] as string[],
    },
  },
  computed: {
    isDataQualityRequired(): boolean {
      return this.currentValue !== "";
    },
  },
  data() {
    return {
      qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
        label: qualityOption,
        value: qualityOption,
      })),
      currentValue: "",
    };
  },
  props: {
    ...YesNoFormFieldProps,
    options: {
      type: Array,
    },
    placeholder: {
      type: String,
    },
  },
  methods: {
    selectNothingIfNotExistsFormKitPlugin,
  },
});
</script>
