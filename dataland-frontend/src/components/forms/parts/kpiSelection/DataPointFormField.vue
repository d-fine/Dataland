<template>
  <DataPointHeader :label="label" :description="description" :is-required="required" />

    <div class="form-field" >
      <div v-if="valueType === 'percent' && showSecondInput" class="p-0">
        <div class="next-to-each-other">
          <div class="p-0">
            <UploadFormHeader
              :label="valueType === 'percent' ? `${label} (%)` : `${label}`"
              :description="description ?? ''"
            />
            <FormKit
              type="number"
              name="valueAsPercentage"
              validation-label=""
              v-model="currentPercentageValue"
              :placeholder="valueType === 'percent' ? 'Value %' : 'Value'"
              step="any"
              min="0"
              :validation="valueType === 'percent' ? 'number|between:0,100' : 'number'"
              :inner-class="{
                short: false,
              }"
            />
          </div>
          <div>
            <UploadFormHeader
              :label="`${label}`"
              :description="`${describtion}Amount` ?? ''"
            />
            <FormKit
              type="number"
              name="valueAsAbsolute"
              validation-label=""
              v-model="currentAmountValue"
              :placeholder="'Value'"
              step="any"
              min="0"
              :validation="'number'"
              :inner-class="{
                short: false,
              }"
            />
          </div>
        </div>
      </div>
      <div v-else>
        <UploadFormHeader
          :label="valueType === 'percent' ? `${label} (%)` : `${label}`"
          :description="describtion ?? ''"
        />
        <FormKit
          type="number"
          name="value"
          validation-label=""
          v-model="currentAmountValue"
          :placeholder="'Value'"
          step="any"
          min="0"
          :validation="'number'"
          :inner-class="{
            short: true,
          }"
        />
      </div>
    </div>
    <!--
    <div class="form-field">
      <FormKit type="group"  name="dataSource">
        <h4 class="mt-0">Data source</h4>
        <div class="next-to-each-other">
          <div class="flex-1">
            <UploadFormHeader :label="kpiNameMappings.report ?? ''" :description="kpiInfoMappings.report ?? ''" />
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
            <UploadFormHeader :label="kpiNameMappings.page ?? ''" :description="kpiInfoMappings.page ?? ''" />
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
-->
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
          :validation=" 'required'"
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
import InputSwitch from "primevue/inputswitch";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import { QualityOptions } from "@clients/backend";
import DataPointHeader from "@/components/forms/parts/kpiSelection/DataPointHeader.vue";
import { selectNothingIfNotExistsFormKitPlugin } from "@/utils/FormKitPlugins";

export default defineComponent({
  name: "DataPointFormField",
  components: { DataPointHeader, UploadFormHeader, FormKit, InputSwitch },
  data: () => ({
    qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
      label: qualityOption,
      value: qualityOption,
    })),
    currentAmountValue: "",
    currentPercentageValue: "",
    currentReportValue: "",
    currentPageValue: "",
    currentQualityValue: "",
    qualityValueBeforeDataPointWasDisabled: "",
  }),
  props: {
    name: {
      type: String,
    },
    kpiInfoMappings: {
      type: Object,
      default: null,
    },
    kpiNameMappings: {
      type: Object,
      default: null,
    },
    valueType: {
      type: String as () => "percent" | "number",
      default: "percent",
    },
    reportsName: {
      type: Array,
      default: () => [],
    },
    showSecondInput: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    selectNothingIfNotExistsFormKitPlugin,
  },
});
</script>
