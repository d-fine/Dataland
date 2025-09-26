<template>
  <div :data-test="dataTest" class="mb-3 p-0 -ml-2">
    <div data-test="toggleDataPointWrapper">
      <div class="px-2 py-3 vertical-middle" v-if="isDataPointToggleable && !isYesNoVariant">
        <ToggleSwitch
          data-test="dataPointToggleButton"
          inputId="dataPointIsAvailableSwitch"
          v-model="dataPointIsAvailable"
        />
        <UploadFormHeader :label="label" :description="description" :is-required="required" />
      </div>
      <div class="px-2 pt-3" v-if="isYesNoVariant">
        <UploadFormHeader :label="label" :description="description" :is-required="required" />
        <div class="yes-no-checkboxes">
          <div v-for="option in options" :key="option.value" class="yes-no-option">
            <Checkbox
              v-model="checkboxValue"
              :inputId="`yes-no-${option.value}`"
              :value="option.value"
              @change="updateYesNoValue()"
            />
            <label :for="`yes-no-${option.value}`">{{ option.label }}</label>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showDataPointFields">
      <FormKit type="group" :name="name" v-model="dataPoint">
        <FormKit
          type="text"
          name="value"
          v-model="yesNoValue"
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
              <UploadFormHeader :label="'Page(s)'" :description="pageNumberDescription" />
              <FormKit
                outer-class="w-100"
                type="text"
                name="page"
                placeholder="Page(s)"
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
                v-model="filteredPageForFileReference"
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
import ToggleSwitch from 'primevue/toggleswitch';
import Checkbox from 'primevue/checkbox';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { FormKit } from '@formkit/vue';
import { QualityOptions } from '@clients/backend';
import { FormFieldPropsWithPlaceholder } from '@/components/forms/parts/fields/FormFieldProps';
import { type ObjectType } from '@/utils/UpdateObjectUtils';
import { getAvailableFileNames, getFileReferenceByFileName, PAGE_NUMBER_DESCRIPTION } from '@/utils/FileUploadUtils';
import { type ExtendedDataPoint } from '@/utils/DataPoint';
import { isValidFileName, noReportLabel } from '@/utils/DataSource';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import type { DropdownOption } from '@/utils/PremadeDropdownDatasets';

export default defineComponent({
  name: 'ExtendedDataPointFormField',
  components: { SingleSelectFormElement, UploadFormHeader, FormKit, ToggleSwitch, Checkbox },
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
      currentReportValue: undefined as string | undefined,
      dataPoint: {} as ExtendedDataPoint<unknown>,
      currentValue: undefined as string | undefined,
      checkboxValue: [] as Array<string>,
      firstAssignmentWhileEditModeWasDone: false,
      pageForFileReference: undefined as string | undefined,
      isValidFileName: isValidFileName,
      yesNoValue: undefined as string | undefined,
    };
  },
  mounted() {
    void nextTick(() => (this.isMounted = true));
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
    filteredPageForFileReference: {
      get() {
        return this.pageForFileReference === '' ? undefined : this.pageForFileReference;
      },

      set(newValue: undefined | string) {
        this.pageForFileReference = newValue;
      },
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
      if (this.firstAssignmentWhileEditModeWasDone) {
        return;
      } else {
        this.setCheckboxValue(newVal);
        this.firstAssignmentWhileEditModeWasDone = true;
      }
    },

    checkboxValue(newArr: string[]) {
      if (newArr.length > 1) {
        const last = newArr[newArr.length - 1];
        this.checkboxValue = [last];
        this.yesNoValue = last;
      } else if (newArr.length === 1) {
        const [only] = newArr;
        if (this.yesNoValue !== only) {
          this.yesNoValue = only;
        }
      } else {
        this.yesNoValue = undefined;
      }
    },
  },
  methods: {
    validatePageNumber,

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
     * updateCurrentValue
     */
    updateYesNoValue() {
      if (this.checkboxValue.length) {
        this.dataPointIsAvailable = true;
      } else {
        this.dataPointIsAvailable = false;
        this.yesNoValue = undefined;
      }
    },
  },
});
</script>
<style scoped>
.vertical-middle {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.yes-no-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.yes-no-checkboxes {
  display: flex; /* lay children out in a row */
  gap: 7rem; /* space between each checkbox+label */
  align-items: center; /* vertical align if labels differ in height */
}

.yes-no-checkboxes input[type='checkbox']:hover {
  /* pointer cursor on the box itself */
  cursor: pointer;
}

.yes-no-checkboxes label {
  /* smooth transition if you like */
  transition: background-color 0.2s ease;
}

.yes-no-checkboxes label:hover {
  /* pointer + background on hover */
  cursor: pointer;
  background-color: rgba(0, 0, 0, 0.05);
}
</style>
