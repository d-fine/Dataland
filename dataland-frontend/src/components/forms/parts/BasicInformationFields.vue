<template>
  <div class="uploadFormSection">
    <div class="col-3 p-3 topicLabel">
      <h4 id="basicInformation" class="anchor title">Basic information</h4>
    </div>
    <!-- Basic information -->
    <div class="col-9 formFields">
      <h3 class="mt-0">Basic information</h3>

      <YesNoComponent
        :displayName="euTaxonomyKpiNameMappings.fiscalYearDeviation"
        :info="euTaxonomyKpiInfoMappings.fiscalYearDeviation"
        :name="'fiscalYearDeviation'"
        :radioButtonsOptions="['Deviation', 'NoDeviation']"
        required="required"
      />

      <!-- The date the fiscal year ends -->
      <div class="form-field">
        <UploadFormHeader
          :name="euTaxonomyKpiNameMappings.fiscalYearEnd"
          :explanation="euTaxonomyKpiInfoMappings.fiscalYearEnd"
        />
        <div class="lg:col-6 md:col-6 col-12 p-0">
          <Calendar
            inputId="fiscalYearEnd"
            v-model="fiscalYearEnd"
            data-test="fiscalYearEnd"
            :showIcon="true"
            dateFormat="D, M dd, yy"
          />
        </div>

        <FormKit
          type="hidden"
          validation="required"
          validation-label="Fiscal year"
          name="fiscalYearEnd"
          v-model="convertedFiscalYearEnd"
        />
      </div>

      <!-- Scope of entities -->
      <div class="form-field">
        <YesNoComponent
          :displayName="euTaxonomyKpiNameMappings.scopeOfEntities"
          :info="euTaxonomyKpiInfoMappings.scopeOfEntities"
          :name="'scopeOfEntities'"
        />
      </div>

      <!-- EU Taxonomy activity level reporting -->
      <div class="form-field">
        <YesNoComponent
          :displayName="euTaxonomyKpiNameMappings.activityLevelReporting"
          :info="euTaxonomyKpiInfoMappings.activityLevelReporting"
          :name="'activityLevelReporting'"
        />
      </div>

      <!-- Number of employees -->
      <div class="form-field">
        <UploadFormHeader
          :name="euTaxonomyKpiNameMappings.numberOfEmployees"
          :explanation="euTaxonomyKpiInfoMappings.numberOfEmployees"
        />
        <div class="lg:col-4 md:col-4 col-6 p-0">
          <FormKit
            type="number"
            name="numberOfEmployees"
            validation-label="Number of employees"
            placeholder="Value"
            validation="required|number"
            step="1"
            min="0"
          />
        </div>
      </div>

      <!-- EU Taxonomy activity level reporting -->
      <div class="form-field">
        <YesNoComponent
          :displayName="euTaxonomyKpiNameMappings.reportingObligation"
          :info="euTaxonomyKpiInfoMappings.reportingObligation"
          :name="'reportingObligation'"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import { defineComponent } from "vue";
import YesNoComponent from "@/components/forms/parts/YesNoComponent.vue";
import { FormKit } from "@formkit/vue";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import Calendar from "primevue/calendar";

export default defineComponent({
  name: "BasicInformationFields",
  components: { UploadFormHeader, Calendar, FormKit, YesNoComponent },
  data: () => ({
    fiscalYearEnd: "" as Date | "",
    convertedFiscalYearEnd: "",
  }),
  watch: {
    fiscalYearEnd: function (newValue: Date) {
      if (newValue) {
        this.convertedFiscalYearEnd = getHyphenatedDate(newValue);
      } else {
        this.convertedFiscalYearEnd = "";
      }
    },
  },
  props: {
    euTaxonomyKpiNameMappings: {},
    euTaxonomyKpiInfoMappings: {},
  },
});
</script>
