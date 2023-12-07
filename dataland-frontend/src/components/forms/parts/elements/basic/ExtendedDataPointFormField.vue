<template>
  <div class="mb-3 p-0 -ml-2" :class="dataPointIsAvailable ? 'bordered-box' : ''">
    <div class="px-2 py-3 next-to-each-other vertical-middle" v-if="isDataPointToggleable">
      <InputSwitch
        data-test="dataPointToggleButton"
        inputId="dataPointIsAvailableSwitch"
        @click="dataPointAvailableToggle"
        v-model="dataPointIsAvailable"
      />
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
    </div>


    <div class="" v-if="Object.keys(options).length">
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
      <FormKit
          type="checkbox"
          name="name"
          v-model="checkboxValue"
          :options="options"
          :outer-class="{
          'yes-no-radio': true,
        }"
          :inner-class="{
          'formkit-inner': false,
        }"
          :input-class="{
          'formkit-input': false,
          'p-radiobutton': true,
        }"
          :ignore="true"
          :plugins="[disabledOnMoreThanOne]"
          @input="updateCurrentValue($event)"
      />
    </div>

    <div class="p-2">
      <FormKit type="group" :name="name" v-model="dataPoint">
        <FormKit
            type="text"
            name="value"
            v-model="currentValue"
            :validation="validation"
            :validation-label="validationLabel"
            :outer-class="{ 'hidden-input': true, 'formkit-outer': false, }"
            v-if="Object.keys(options).length"
        />
        <div class="col-12"  v-if="dataPoint.value === 'Yes'">
          <UploadFormHeader
            v-if="!isDataPointToggleable"
            :label="label"
            :description="description"
            :is-required="required"
          />
          <slot />
          <div class="grid align-content-end">
            <FormKit type="group" name="dataSource">
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
                  :options="['None...', ...reportsName]"
                />
                <FormKit type="hidden" name="fileReference" :modelValue="fileReferenceAccordingToName" />
              </div>
              <div class="col-4">
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
              :disabled="!isDataQualityRequired"
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
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import InputSwitch from "primevue/inputswitch";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import { QualityOptions } from "@clients/backend";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { type ObjectType } from "@/utils/UpdateObjectUtils";
import { getFileName, getFileReferenceByFileName } from "@/utils/FileUploadUtils";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { disabledOnMoreThanOne } from "@/utils/FormKitPlugins";
import {ExtendedDataPoint} from "@/utils/DataPoint";

export default defineComponent({
  name: "ExtendedDataPointFormField",
  components: { UploadFormHeader, FormKit, InputSwitch },
  inject: {
    injectReportsNameAndReferences: {
      from: "namesAndReferencesOfAllCompanyReportsForTheDataset",
      default: {} as ObjectType,
    },
    injectlistOfFilledKpis: {
      from: "listOfFilledKpis",
      default: [] as Array<string>,
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
    showDataPointFields(): boolean {
      return !this.isDataPointToggleable || this.dataPointIsAvailable;
    },
  },
  data() {
    return {
      dataPointIsAvailable: (this.injectlistOfFilledKpis as unknown as Array<string>).includes(this.name as string),
      qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
        label: qualityOption,
        value: qualityOption,
      })),
      qualityValue: "NA",
      currentReportValue: "",
      dataPoint: {} as ExtendedDataPoint<unknown>,
      dataPointValuesBeforeDataPointWasDisabled: {},
      currentValue: "",
      checkboxValue: [],
    };
  },

  props: {
    ...BaseFormFieldProps,
    checkValueValidity: {
      type: Function as unknown as () => (dataPoint: unknown) => boolean,
      default: (): boolean => false,
    },
    isDataPointToggleable: {
      type: Boolean,
      default: true,
    },
    options: {
      type: Object,
      default: () => ({}),
    },
  },
  watch: {
    isDataValueProvided(isDataValueProvided: boolean) {
      this.handleBlurValue(isDataValueProvided);
    },
    dataPointIsAvailable(newValue: boolean) {
      if (!newValue) {
        this.dataPointValuesBeforeDataPointWasDisabled = this.dataPoint;
      } else {
        this.dataPoint = this.dataPointValuesBeforeDataPointWasDisabled;
      }
    },
  },
  methods: {
    disabledOnMoreThanOne,
    /**
     * Handle blur event on value input.
     * @param isDataValueProvided boolean which gives information whether data is provided or not
     */
    handleBlurValue(isDataValueProvided: boolean) {
      if (!isDataValueProvided) {
        this.qualityValue = QualityOptions.Na;
      } else if (this.qualityValue === QualityOptions.Na) {
        this.qualityValue = "";
      }
    },
    /**
     * Toggle dataPointIsAvailable variable value
     */
    dataPointAvailableToggle(): void {
      this.dataPointIsAvailable = !this.dataPointIsAvailable;
    },
    /**
     * updateCurrentValue
     * @param checkboxValue checkboxValue
     */
    updateCurrentValue(checkboxValue: [string]) {
      console.log("checkboxValue BaseDataPointFormField", checkboxValue[0]);
      if (checkboxValue[0]) {
        this.dataPointIsAvailable = true;
        this.dataPoint.value = checkboxValue[0].toString();
        console.log("this.baseDataPoint.value +++", this.dataPoint.value);
      } else {
        this.dataPointIsAvailable = false;
      }
    },
  },
});
</script>
