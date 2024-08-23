<template>
  <FormKit name="assurance" type="group">
    <!-- Level of assurance -->
    <div class="form-field">
      <div class="lg:col-4 md:col-6 col-12 p-0 formkit-outer normal-line-height">
        <SingleSelectFormField
          :label="euTaxonomyKpiNameMappings.assurance ?? ''"
          :description="euTaxonomyKpiInfoMappings.assurance ?? ''"
          :required="required"
          name="value"
          placeholder="Please choose..."
          :validation-label="euTaxonomyKpiNameMappings.assurance ?? ''"
          :validation="validation"
          :options="assuranceData"
        />
      </div>
    </div>
    <!-- Assurance provider -->
    <div class="form-field">
      <UploadFormHeader
        :label="euTaxonomyKpiNameMappings.provider ?? ''"
        :description="euTaxonomyKpiInfoMappings.provider ?? ''"
      />
      <FormKit
        type="text"
        name="provider"
        :placeholder="euTaxonomyKpiNameMappings.provider ?? ''"
        :validation-label="euTaxonomyKpiNameMappings.provider ?? ''"
      />
    </div>

    <!-- Data source -->
    <div class="form-field">
      <h4 class="mt-0">Data source</h4>
      <div class="next-to-each-other">
        <div class="flex-1">
          <SingleSelectFormField
            :label="euTaxonomyKpiNameMappings.report ?? ''"
            :description="euTaxonomyKpiInfoMappings.report ?? ''"
            :required="required"
            v-model="currentReportValue"
            placeholder="Select a report"
            :options="[noReportLabel, ...reportsName]"
            name="fileName"
            allow-unknown-option
            ignore
          />
        </div>
        <div>
          <UploadFormHeader
            :label="euTaxonomyKpiNameMappings.page ?? ''"
            :description="euTaxonomyKpiInfoMappings.page ?? ''"
          />
          <FormKit
            name="page"
            v-model="reportPageNumber"
            outer-class="w-100"
            type="number"
            placeholder="Page"
            validation-label="Page"
            validation="min:0"
            step="1"
            min="0"
            ignore="true"
          />
          <FormKit type="group" name="dataSource" v-if="isValidFileName(isMounted, currentReportValue)">
            <FormKit type="hidden" name="fileName" v-model="currentReportValue" />
            <FormKit type="hidden" name="fileReference" :modelValue="fileReferenceAccordingToName" />
            <FormKit type="hidden" name="page" v-model="reportPageNumber" />
          </FormKit>
        </div>
      </div>
    </div>
  </FormKit>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent, nextTick } from 'vue';
import { FormKit } from '@formkit/vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from '@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { AssuranceDataPointValueEnum } from '@clients/backend';
import { type ObjectType } from '@/utils/UpdateObjectUtils';
import { getFileName, getFileReferenceByFileName } from '@/utils/FileUploadUtils';
import { isValidFileName, noReportLabel } from '@/utils/DataSource';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';

export default defineComponent({
  name: 'AssuranceFormField',
  inject: {
    injectReportsNameAndReferences: {
      from: 'namesAndReferencesOfAllCompanyReportsForTheDataset',
      default: {} as ObjectType,
    },
  },
  components: { SingleSelectFormField, FormKit, UploadFormHeader },
  data() {
    return {
      isMounted: false,
      euTaxonomyKpiNameMappings,
      euTaxonomyKpiInfoMappings,
      assuranceData: {
        None: humanizeStringOrNumber(AssuranceDataPointValueEnum.None),
        LimitedAssurance: humanizeStringOrNumber(AssuranceDataPointValueEnum.LimitedAssurance),
        ReasonableAssurance: humanizeStringOrNumber(AssuranceDataPointValueEnum.ReasonableAssurance),
      },
      currentReportValue: '',
      reportPageNumber: undefined as string | undefined,
      noReportLabel: noReportLabel,
      isValidFileName: isValidFileName,
    };
  },
  mounted() {
    void nextTick(() => (this.isMounted = true));
  },
  computed: {
    reportsName(): string[] {
      return getFileName(this.injectReportsNameAndReferences);
    },
    fileReferenceAccordingToName(): string {
      return getFileReferenceByFileName(this.currentReportValue, this.injectReportsNameAndReferences);
    },
  },
  props: BaseFormFieldProps,
});
</script>
