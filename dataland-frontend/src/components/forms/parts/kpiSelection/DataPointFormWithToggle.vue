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
      {{ dataPointIsAvailable ? 'Data point is available' : 'Data point is not available' }}
    </h5>
  </div>
  <div v-show="dataPointIsAvailable">
    <div class="form-field" v-if="dataPointIsAvailable">
      <div v-if="valueType === 'percent' && showSecondInput" class="p-0">
        <div class="next-to-each-other">
          <div class="p-0">
            <UploadFormHeader
              :label="valueType === 'percent' ? `${kpiNameMappings[name]} (%)` : `${kpiNameMappings[name]}`"
              :description="kpiInfoMappings[name] ?? ''"
            />
            <FormKit
              :disabled="!dataPointIsAvailable"
              type="number"
              data-test="valueAsPercentageInSecondInputMode"
              name="valueAsPercentage"
              validation-label="Value"
              v-model="currentValue"
              :placeholder="valueType === 'percent' ? 'Value %' : 'Value'"
              step="any"
              min="0"
              validation="number"
              :inner-class="{
                short: false,
              }"
            />
          </div>
          <div>
            <UploadFormHeader
              :label="`${kpiNameMappings[name]}`"
              :description="kpiInfoMappings[`${name}Amount`] ?? ''"
            />
            <FormKit
              :disabled="!dataPointIsAvailable"
              type="number"
              data-test="valueAsAbsoluteInSecondInputMode"
              name="valueAsAbsolute"
              validation-label="Value"
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
          :label="valueType === 'percent' ? `${kpiNameMappings[name]} (%)` : `${kpiNameMappings[name]}`"
          :description="kpiInfoMappings[name] ?? ''"
        />
        <FormKit
          :disabled="!dataPointIsAvailable"
          type="number"
          data-test="value"
          name="value"
          validation-label="Value"
          v-model="currentValue"
          :placeholder="valueType === 'percent' ? 'Value %' : 'Value'"
          step="any"
          min="0"
          validation="number"
          :inner-class="{
            short: true,
          }"
        />
      </div>
    </div>
    <div class="form-field">
      <h4 class="mt-0">Data source</h4>
      <div class="next-to-each-other">
        <div class="flex-1">
          <UploadFormHeader :label="kpiNameMappings.report ?? ''" :description="kpiInfoMappings.report ?? ''" />
          <SingleSelectFormElement
            name="fileName"
            :disabled="!dataPointIsAvailable"
            v-model="currentReportValue"
            placeholder="Select a report"
            :options="[noReportLabel, ...reportsName]"
            ignore
            allow-unknown-option
          />
        </div>
        <div>
          <UploadFormHeader :label="kpiNameMappings.page ?? ''" :description="kpiInfoMappings.page ?? ''" />
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
            ignore="true"
          />
        </div>
      </div>
      <FormKit v-if="isValidFileName(isMounted, currentReportValue)" type="group" name="dataSource">
        <FormKit type="hidden" name="fileName" v-model="currentReportValue" />
        <FormKit type="hidden" name="fileReference" :modelValue="fileReferenceAccordingToName" />
        <FormKit type="hidden" name="page" v-model="currentPageValue" />
      </FormKit>
    </div>

    <!-- Data quality -->
    <div class="form-field">
      <UploadFormHeader label="Data quality" description="The level of confidence associated to the value." />
      <div class="md:col-6 col-12 p-0">
        <SingleSelectFormElement
          :disabled="!dataPointIsAvailable"
          data-test="qualityValue"
          v-model="currentQualityValue"
          name="quality"
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
// @ts-nocheck
import { defineComponent } from 'vue';
import InputSwitch from 'primevue/inputswitch';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { FormKit } from '@formkit/vue';
import { QualityOptions } from '@clients/backend';
import DataPointHeader from '@/components/forms/parts/kpiSelection/DataPointHeader.vue';
import { getFileName, getFileReferenceByFileName } from '@/utils/FileUploadUtils';
import { isValidFileName, noReportLabel } from '@/utils/DataSource';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';

export default defineComponent({
  name: 'DataPointFormWithToggle',
  components: { SingleSelectFormElement, DataPointHeader, UploadFormHeader, FormKit, InputSwitch },
  emits: ['dataPointAvailableToggle'],
  data: () => ({
    isMounted: false,
    dataPointIsAvailable: true,
    qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
      label: qualityOption,
      value: qualityOption,
    })),
    currentAmountValue: '',
    currentValue: '',
    currentReportValue: '',
    currentPageValue: '',
    currentQualityValue: null as string | null,
    amountValueBeforeDataPointWasDisabled: '',
    valueBeforeDataPointWasDisabled: '',
    reportValueBeforeDataPointWasDisabled: '',
    pageValueBeforeDataPointWasDisabled: '',
    qualityValueBeforeDataPointWasDisabled: null as string | null,
    noReportLabel: noReportLabel,
    isValidFileName: isValidFileName,
  }),
  watch: {
    dataPointIsAvailable(newValue: boolean) {
      if (!newValue) {
        this.amountValueBeforeDataPointWasDisabled = this.currentAmountValue;
        this.valueBeforeDataPointWasDisabled = this.currentValue;
        this.reportValueBeforeDataPointWasDisabled = this.currentReportValue;
        this.pageValueBeforeDataPointWasDisabled = this.currentPageValue;
        this.qualityValueBeforeDataPointWasDisabled = this.currentQualityValue;
        this.currentAmountValue = '';
        this.currentValue = '';
        this.currentReportValue = '';
        this.currentPageValue = '';
        this.currentQualityValue = null;
      } else {
        this.currentQualityValue = this.qualityValueBeforeDataPointWasDisabled;
        this.currentPageValue = this.pageValueBeforeDataPointWasDisabled;
        this.currentReportValue = this.reportValueBeforeDataPointWasDisabled;
        this.currentValue = this.valueBeforeDataPointWasDisabled;
        this.currentAmountValue = this.amountValueBeforeDataPointWasDisabled;
      }
    },
  },
  mounted() {
    setTimeout(() => (this.isMounted = true));
  },
  computed: {
    reportsName(): string[] {
      return getFileName(this.reportsNameAndReferences);
    },
    fileReferenceAccordingToName() {
      return getFileReferenceByFileName(this.currentReportValue, this.reportsNameAndReferences);
    },
  },
  props: {
    name: {
      type: String,
      required: true,
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
      type: String as () => 'percent' | 'number',
      default: 'percent',
    },
    reportsNameAndReferences: {
      type: Object,
      default: () => ({}),
    },
    showSecondInput: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
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
