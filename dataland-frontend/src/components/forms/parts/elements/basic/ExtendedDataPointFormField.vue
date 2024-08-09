<template>
  <div :data-test="dataTest" class="mb-3 p-0 -ml-2" :class="showDataPointFields ? 'bordered-box' : ''">
    <div data-test="toggleDataPointWrapper">
      <div class="px-2 py-3 next-to-each-other vertical-middle" v-if="isDataPointToggleable && !isYesNoVariant">
        <InputSwitch
          data-test="dataPointToggleButton"
          inputId="dataPointIsAvailableSwitch"
          @click="dataPointAvailableToggle"
          v-model="dataPointIsAvailable"
        />
        <UploadFormHeader :label="label" :description="description" :is-required="required" />
      </div>
      <div class="px-2 pt-3" v-if="isYesNoVariant">
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
    </div>

    <div v-if="showDataPointFields">
      <FormKit type="group" :name="name" v-model="dataPoint">
        <FormKit
          type="text"
          name="value"
          v-model="currentValue"
          :placeholder="placeholder"
          :validation="validation"
          :validation-label="validationLabel"
          :outer-class="{ 'hidden-input': true, 'formkit-outer': false }"
          v-if="isYesNoVariant"
        />
        <div class="col-12" v-if="dataPoint.value || !isYesNoVariant">
          <UploadFormHeader
            v-if="!isDataPointToggleable && !isYesNoVariant"
            :label="label"
            :description="description"
            :is-required="required"
          />
          <slot v-if="!isYesNoVariant" />
          <div class="grid align-content-end">
            <div class="col-8">
              <UploadFormHeader
                :label="`${label} Report`"
                description="Select a report as a reference for this data point."
              />
              <SingleSelectFormElement
                name="fileName"
                v-model="currentReportValue"
                placeholder="Select a report"
                :options="[noReportLabel, ...reportsName]"
                allow-unknown-option
                ignore
                input-class="w-12"
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
                ignore="true"
              />
            </div>

            <FormKit v-if="isValidFileName(isMounted, currentReportValue)" type="group" name="dataSource">
              <FormKit type="hidden" name="fileName" v-model="currentReportValue" />
              <FormKit type="hidden" name="fileReference" :modelValue="fileReferenceAccordingToName" />
              <FormKit type="hidden" name="page" v-model="pageForFileReference" />
            </FormKit>
          </div>

          <!-- Data quality -->
          <div class="md:col-8 col-12 p-0 mb-4" data-test="dataQuality">
            <UploadFormHeader
              :label="`${label} Quality`"
              description="The level of confidence associated to the value."
            />
            <SingleSelectFormElement
              name="quality"
              v-model="qualityValue"
              :disabled="!isDataQualityRequired"
              validation-label="Data quality"
              placeholder="Data quality"
              :options="computeQualityOption"
            />
          </div>
          <div class="form-field">
            <FormKit
              type="textarea"
              name="comment"
              v-model="commentValue"
              placeholder="(Optional) Add comment that might help Quality Assurance to approve the datapoint. "
            />
          </div>
        </div>
      </FormKit>
    </div>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent, nextTick } from 'vue';
import InputSwitch from 'primevue/inputswitch';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { FormKit } from '@formkit/vue';
import { QualityOptions } from '@clients/backend';
import { FormFieldPropsWithPlaceholder } from '@/components/forms/parts/fields/FormFieldProps';
import { type ObjectType } from '@/utils/UpdateObjectUtils';
import { getFileName, getFileReferenceByFileName } from '@/utils/FileUploadUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { disabledOnMoreThanOne } from '@/utils/FormKitPlugins';
import { type ExtendedDataPoint } from '@/utils/DataPoint';
import { isValidFileName, noReportLabel } from '@/utils/DataSource';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';

export default defineComponent({
  name: 'ExtendedDataPointFormField',
  components: { SingleSelectFormElement, UploadFormHeader, FormKit, InputSwitch },
  inject: {
    injectReportsNameAndReferences: {
      from: 'namesAndReferencesOfAllCompanyReportsForTheDataset',
      default: {} as ObjectType,
    },
    injectlistOfFilledKpis: {
      from: 'listOfFilledKpis',
      default: [] as Array<string>,
    },
  },
  data() {
    return {
      isMounted: false,
      dataPointIsAvailable: (this.injectlistOfFilledKpis as unknown as Array<string>).includes(this.name as string),
      qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
        label: humanizeStringOrNumber(qualityOption),
        value: qualityOption,
      })),
      qualityValue: null as null | string,
      commentValue: '',
      currentReportValue: null as string | null,
      dataPoint: {} as ExtendedDataPoint<unknown>,
      currentValue: null,
      checkboxValue: [] as Array<string>,
      firstAssignmentWhileEditModeWasDone: false,
      noReportLabel: noReportLabel,
      pageForFileReference: undefined as string | undefined,
      isValidFileName: isValidFileName,
    };
  },
  mounted() {
    void nextTick(() => (this.isMounted = true));
  },
  computed: {
    showDataPointFields(): boolean {
      return this.dataPointIsAvailable || !this.isDataPointToggleable;
    },
    isDataValueProvided(): boolean {
      return (assertDefined(this.checkValueValidity) as (dataPoint: unknown) => boolean)(this.dataPoint);
    },
    isDataQualityRequired(): boolean {
      return this.isDataValueProvided;
    },
    computeQualityOption(): object {
      return this.qualityOptions;
    },
    reportsName(): string[] {
      return getFileName(this.injectReportsNameAndReferences);
    },
    fileReferenceAccordingToName(): string {
      return getFileReferenceByFileName(this.currentReportValue, this.injectReportsNameAndReferences);
    },
    isYesNoVariant() {
      return Object.keys(this.options).length;
    },
  },
  props: {
    ...FormFieldPropsWithPlaceholder,
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
    dataTest: {
      type: String,
      default: '',
    },
  },
  watch: {
    isDataValueProvided(isDataValueProvided: boolean) {
      this.handleBlurValue(isDataValueProvided);
    },
    currentValue(newVal: string) {
      if (!this.firstAssignmentWhileEditModeWasDone) {
        this.setCheckboxValue(newVal);
        this.firstAssignmentWhileEditModeWasDone = true;
      } else {
        return;
      }
    },
  },
  methods: {
    disabledOnMoreThanOne,

    /**
     * A function that rewrite value to select the appropriate checkbox
     * @param newCheckboxValue value after changing value that must be reflected in checkboxes
     */
    setCheckboxValue(newCheckboxValue: string) {
      if (newCheckboxValue && newCheckboxValue !== '') {
        this.checkboxValue = [newCheckboxValue];
      }
    },
    /**
     * Handle blur event on value input.
     * @param isDataValueProvided boolean which gives information whether data is provided or not
     */
    handleBlurValue(isDataValueProvided: boolean) {
      if (!isDataValueProvided && !this.isYesNoVariant) {
        this.qualityValue = null;
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
      if (checkboxValue[0]) {
        this.dataPointIsAvailable = true;
        this.currentValue = checkboxValue[0].toString();
      } else {
        this.dataPointIsAvailable = false;
        this.currentValue = null;
        this.dataPoint = {};
      }
    },
  },
});
</script>
