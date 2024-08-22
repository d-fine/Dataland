<template>
  <div class="uploadFormSection">
    <div class="col-3 p-3 topicLabel">
      <h4 id="basicInformation" class="anchor title">Basic information</h4>
    </div>
    <!-- Basic information -->
    <div class="col-9 formFields">
      <h3 class="mt-0">Basic information</h3>
      <RadioButtonsFormField
        :name="'fiscalYearDeviation'"
        :description="euTaxonomyKpiInfoMappings.fiscalYearDeviation"
        :label="euTaxonomyKpiNameMappings.fiscalYearDeviation"
        :options="{
          Deviation: 'Deviation',
          NoDeviation: 'No Deviation',
        }"
      />

      <!-- The date the fiscal year ends -->
      <div class="form-field">
        <UploadFormHeader
          :label="euTaxonomyKpiNameMappings.fiscalYearEnd"
          :description="euTaxonomyKpiInfoMappings.fiscalYearEnd"
        />
        <div class="md:col-6 col-12 p-0">
          <Calendar
            inputId="fiscalYearEndAsDate"
            :modelValue="fiscalYearEndAsDate"
            data-test="fiscalYearEnd"
            :showIcon="true"
            dateFormat="D, M dd, yy"
            @update:modelValue="updateFiscalYearEndHandler($event)"
          />
        </div>

        <FormKit
          type="text"
          validation-label="Fiscal year"
          :modelValue="fiscalYearEnd"
          name="fiscalYearEnd"
          :outer-class="{ 'hidden-input': true }"
        />
      </div>

      <!-- Scope of entities -->
      <div class="form-field">
        <YesNoFormField
          :name="'scopeOfEntities'"
          :description="euTaxonomyKpiInfoMappings.scopeOfEntities"
          :label="euTaxonomyKpiNameMappings.scopeOfEntities"
        />
      </div>

      <!-- EU Taxonomy activity level reporting -->
      <div class="form-field">
        <YesNoFormField
          :name="'euTaxonomyActivityLevelReporting'"
          :description="euTaxonomyKpiInfoMappings.euTaxonomyActivityLevelReporting"
          :label="euTaxonomyKpiNameMappings.euTaxonomyActivityLevelReporting"
        />
      </div>

      <!-- Number of employees -->
      <div class="form-field">
        <UploadFormHeader
          :label="euTaxonomyKpiNameMappings.numberOfEmployees"
          :description="euTaxonomyKpiInfoMappings.numberOfEmployees"
        />
        <div class="lg:col-4 md:col-4 col-6 p-0">
          <FormKit
            type="number"
            name="numberOfEmployees"
            validation-label="Number of employees"
            placeholder="Value"
            validation="number|min:0"
            step="1"
            min="0"
          />
        </div>
      </div>

      <!-- EU Taxonomy activity level reporting -->
      <div class="form-field">
        <YesNoFormField
          :name="'nfrdMandatory'"
          :description="euTaxonomyKpiInfoMappings.nfrdMandatory"
          :label="euTaxonomyKpiNameMappings.nfrdMandatory"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { defineComponent } from 'vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import { FormKit } from '@formkit/vue';
import Calendar from 'primevue/calendar';
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from '@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel';
import RadioButtonsFormField from '@/components/forms/parts/fields/RadioButtonsFormField.vue';

export default defineComponent({
  name: 'EuTaxonomyBasicInformation',
  components: { RadioButtonsFormField, UploadFormHeader, Calendar, FormKit, YesNoFormField },
  emits: ['updateFiscalYearEndHandler'],
  data: () => ({
    euTaxonomyKpiInfoMappings,
    euTaxonomyKpiNameMappings,
  }),
  props: {
    fiscalYearEndAsDate: {
      type: Date,
    },
    fiscalYearEnd: {
      type: String,
    },
  },
  methods: {
    /**
     * Function to emit event to update the Fiscal Year End value
     * @param event new date value
     */
    updateFiscalYearEndHandler(event: Date) {
      this.$emit('updateFiscalYearEndHandler', event);
    },
  },
});
</script>
