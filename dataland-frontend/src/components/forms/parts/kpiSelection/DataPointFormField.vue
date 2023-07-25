<template>
  <DataPointHeader :label="label" :description="description" :is-required="required" />

  <div class="form-field">
    <div class="next-to-each-other">
      <div class="p-0">
        <UploadFormHeader :label="label" :description="description ?? ''" />
        <FormKit
          type="number"
          :name="name"
          :description="description ?? ''"
          validation-label=""
          v-model="currentValue"
          :placeholder="'Value'"
          step="any"
          min="0"
          :required="required"
          :validation="validation"
          :inner-class="{
            short: false,
          }"
        />
      </div>
    </div>
  </div>
  <!-- //TODO make the label and description modular instead of being hardcoded -->
  <div class="form-field">
    <FormKit type="group" name="dataSource">
      <div class="next-to-each-other">
        <div class="flex-1">
          <UploadFormHeader :label="'Report'" :description="'Upload Report'" />
          <FormKit
            type="select"
            name="report"
            v-model="currentReportValue"
            placeholder="Select a report"
            :options="['None...', ...reportsName]"
            :plugins="[selectNothingIfNotExistsFormKitPlugin]"
          />
        </div>
        <div>
          <UploadFormHeader :label="'Page'" :description="'Page where information was found'" />
          <FormKit
            outer-class="w-100"
            v-model="currentPageValue"
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
  <div class="form-field">
    <UploadFormHeader
      label="Data quality"
      description="The level of confidence associated to the value."
      :is-required="true"
    />
    <div class="md:col-6 col-12 p-0">
      <FormKit
        type="select"
        v-model="currentQualityValue"
        name="quality"
        :validation="'required'"
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
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import { QualityOptions } from "@clients/backend";
import DataPointHeader from "@/components/forms/parts/kpiSelection/DataPointHeader.vue";
import { selectNothingIfNotExistsFormKitPlugin } from "@/utils/FormKitPlugins";

export default defineComponent({
  name: "DataPointFormField",
  components: { DataPointHeader, UploadFormHeader, FormKit },
  data: () => ({
    qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
      label: qualityOption,
      value: qualityOption,
    })),
    currentValue: "",
    currentReportValue: "",
    currentPageValue: "",
    currentQualityValue: "",
  }),
  props: {
    label: {
      type: String,
    },
    description: {
      type: String,
    },
    reportsName: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
    selectNothingIfNotExistsFormKitPlugin,
  },
});
</script>
