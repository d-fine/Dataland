<template>
  <DataPointHeader :name="kpiNameMappings[name]" />
  <div data-test="dataPointToggle" class="form-field vertical-middle">
    <InputSwitch
      data-test="dataPointToggleButton"
      inputId="dataPointIsAvailableSwitch"
      @click="dataPointAvailableToggle"
      v-model="dataPointIsAvailable"
    />
    <h5 data-test="dataPointToggleTitle" class="ml-2">
      {{ dataPointIsAvailable ? "Data point is available" : "Data point is not available" }}
    </h5>
  </div>
  <div v-show="dataPointIsAvailable">
    <div class="form-field" v-if="dataPointIsAvailable">
      <UploadFormHeader
        :name="valueType === 'percent' ? `${kpiNameMappings[name]} (%)` : `${kpiNameMappings[name]}`"
        :explanation="kpiInfoMappings[name] ?? ''"
      />
      <FormKit
        :disabled="!dataPointIsAvailable"
        type="number"
        name="value"
        validation-label=""
        v-model="currentMainValue"
        :placeholder="valueType === 'percent' ? 'Value %' : 'Value'"
        step="any"
        min="0"
        :validation="valueType === 'percent' ? 'number|between:0,100' : 'number'"
        :inner-class="{
          short: true,
        }"
      />
    </div>

    <div class="form-field">
      <FormKit type="group" v-if="dataPointIsAvailable" name="dataSource">
        <h4 class="mt-0">Data source</h4>
        <div class="next-to-each-other">
          <div class="flex-1">
            <UploadFormHeader :name="kpiNameMappings.report ?? ''" :explanation="kpiInfoMappings.report ?? ''" />
            <FormKit
              type="select"
              name="report"
              :disabled="!dataPointIsAvailable"
              v-model="currentReportValue"
              placeholder="Select a report"
              :options="['None...', ...reportsName]"
              :plugins="[selectNothingIfNotExistsFormKitPlugin]"
            />
          </div>
          <div>
            <UploadFormHeader :name="kpiNameMappings.page ?? ''" :explanation="kpiInfoMappings.page ?? ''" />
            <FormKit
              outer-class="w-100"
              :disabled="!dataPointIsAvailable"
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
        :is-required="true"
        name="Data quality"
        explanation="The level of confidence associated to the value."
      />
      <div class="md:col-6 col-12 p-0">
        <FormKit
          :disabled="!dataPointIsAvailable"
          type="select"
          v-model="currentQualityValue"
          name="quality"
          :validation="dataPointIsAvailable ? 'required' : ''"
          validation-label="Data quality"
          placeholder="Data quality"
          :options="qualityOptions"
        />
      </div>
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
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import { QualityOptions } from "@clients/backend";
import DataPointHeader from "@/components/forms/parts/kpiSelection/DataPointHeader.vue";
import { selectNothingIfNotExistsFormKitPlugin } from "@/utils/FormKitPlugins";

export default defineComponent({
  name: "DataPointForm",
  components: { DataPointHeader, UploadFormHeader, FormKit, InputSwitch },
  emits: ["dataPointAvailableToggle"],
  data: () => ({
    dataPointIsAvailable: true,
    qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
      label: qualityOption,
      value: qualityOption,
    })),
    currentMainValue: "",
    currentReportValue: "",
    currentPageValue: "",
    currentQualityValue: "",
    qualityValueBeforeDataPointWasDisabled: "",
  }),
  watch: {
    dataPointIsAvailable(newValue: boolean) {
      if (!newValue) {
        this.qualityValueBeforeDataPointWasDisabled = this.currentQualityValue;
        this.currentQualityValue = "NA";
      } else {
        this.currentQualityValue = this.qualityValueBeforeDataPointWasDisabled;
      }
    },
  },
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
    toggleDataAvailable: {
      type: Boolean,
      default: true,
    },
    valueType: {
      type: String as () => "percent" | "number",
      default: "percent",
    },
    reportsName: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
    selectNothingIfNotExistsFormKitPlugin,
    /**
     * Toggle dataPointIsAvailable variable value and emit event
     *
     */
    dataPointAvailableToggle(): void {
      this.dataPointIsAvailable = !this.dataPointIsAvailable;
    },
  },
});
</script>
