<template>
  <div :data-test="dataTest" class="mb-3 p-0 -ml-2" :class="showDataPointFields ? 'bordered-box' : ''">
    <div data-test="toggleDataPointWrapper">
      <div class="px-2 py-3 next-to-each-other vertical-middle" v-if="isDataPointToggleable && !isYesNoVariant">
        <InputSwitch
          data-test="dataPointToggleButton"
          inputId="dataPointIsAvailableSwitch"
          @click="handleToggleClick"
          v-model="dataPointIsAvailable"
        />
        <UploadFormHeader :label="label" :description="description" :is-required="required" />
      </div>
      <div class="px-2 pt-3" v-if="isYesNoVariant">
        <UploadFormHeader :label="label" :description="description" :is-required="required" />
        <FormKit
          type="checkbox"
          name="doesNotMatter"
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
          <div class="grid align-content-end" data-test="dataReport">
            <div class="col-8">
              <UploadFormHeader
                :label="`${label} Report`"
                description="Select a report as a reference for this data point."
              />
              <SingleSelectFormElement
                name="fileName"
                v-model="currentReportValue"
                placeholder="Select a report"
                :options="reportOptions"
                allow-unknown-option
                ignore
                input-class="w-12"
              />
            </div>
            <div v-if="isValidFileName(isMounted, currentReportValue)" class="col-4">
              <UploadFormHeader :label="'Page'" :description="pageNumberDescription" />
              <FormKit
                outer-class="w-100"
                type="text"
                name="page"
                placeholder="Enter page"
                v-model="pageForFileReference"
                :validation-messages="{
                  validatePageNumber: pageNumberValidationErrorMessage,
                }"
                :validation-rules="{ validatePageNumber }"
                validation="validatePageNumber"
                ignore="true"
              />
            </div>

            <FormKit v-if="isValidFileName(isMounted, currentReportValue)" type="group" name="dataSource">
              <FormKit type="hidden" name="fileName" v-model="currentReportValue" />
              <FormKit type="hidden" name="fileReference" :modelValue="fileReferenceAccordingToName" />
              <FormKit
                type="hidden"
                name="page"
                :validation-rules="{ validatePageNumber }"
                validation="validatePageNumber"
                v-model="pageForFileReference"
              />
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
import { defineComponent, nextTick } from 'vue';
import { PAGE_NUMBER_VALIDATION_ERROR_MESSAGE, validatePageNumber } from '@/utils/ValidationUtils';
import InputSwitch from 'primevue/inputswitch';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { FormKit } from '@formkit/vue';
import { QualityOptions } from '@clients/backend';
import { FormFieldPropsWithPlaceholder } from '@/components/forms/parts/fields/FormFieldProps';
import { type ObjectType } from '@/utils/UpdateObjectUtils';
import { getAvailableFileNames, getFileReferenceByFileName, PAGE_NUMBER_DESCRIPTION } from '@/utils/FileUploadUtils';
import { disabledOnMoreThanOne } from '@/utils/FormKitPlugins';
import { type ExtendedDataPoint } from '@/utils/DataPoint';
import { isValidFileName, noReportLabel } from '@/utils/DataSource';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import type { DropdownOption } from '@/utils/PremadeDropdownDatasets';

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
      pageNumberDescription: PAGE_NUMBER_DESCRIPTION,
      pageNumberValidationErrorMessage: PAGE_NUMBER_VALIDATION_ERROR_MESSAGE,
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
      currentValue: null as string | null,
      checkboxValue: [] as Array<string>,
      firstAssignmentWhileEditModeWasDone: false,
      pageForFileReference: undefined as string | undefined,
      isValidFileName: isValidFileName,
    };
  },
  mounted() {
    nextTick(() => (this.isMounted = true));
  },
  computed: {
    showDataPointFields(): boolean {
      return this.dataPointIsAvailable || !this.isDataPointToggleable;
    },
    computeQualityOption(): DropdownOption[] {
      return this.qualityOptions;
    },
    reportOptions(): DropdownOption[] {
      const plainOptions = [noReportLabel, ...getAvailableFileNames(this.injectReportsNameAndReferences as ObjectType)];
      return plainOptions.map((it) => ({ value: it, label: it }));
    },
    fileReferenceAccordingToName(): string {
      return getFileReferenceByFileName(this.currentReportValue, this.injectReportsNameAndReferences as ObjectType);
    },
    isYesNoVariant() {
      return Object.keys(this.options).length;
    },
  },
  props: {
    ...FormFieldPropsWithPlaceholder,
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
    validatePageNumber,
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
     * Toggle dataPointIsAvailable variable value
     */
    handleToggleClick(): void {
      this.dataPointIsAvailable = !this.dataPointIsAvailable;
    },
    /**
     * updateCurrentValue
     * @param checkboxValue checkboxValue
     */
    updateCurrentValue(checkboxValue: string[] | undefined) {
      if (checkboxValue && checkboxValue[0]) {
        this.dataPointIsAvailable = true;
        this.currentValue = checkboxValue[0];
      } else {
        this.dataPointIsAvailable = false;
        this.currentValue = null;
        this.dataPoint = {};
      }
    },
  },
});
</script>
