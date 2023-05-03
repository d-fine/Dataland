<template>
  <FormKit :name="name" type="group">
    <!-- Date of the report -->
    <div class="form-field">
      <UploadFormHeader
        :name="euTaxonomyKpiNameMappings?.reportDate ?? 'Report Date'"
        :explanation="euTaxonomyKpiInfoMappings?.reportDate ?? 'Report Date'"
      />
      <div class="md:col-6 col-12 p-0">
        <Calendar
          data-test="reportDate"
          inputId="icon"
          :modelValue="reportDateAsDate"
          :showIcon="true"
          dateFormat="D, M dd, yy"
          @update:modelValue="reportingDateChanged($event)"
        />
      </div>
      <FormKit type="text" :modelValue="hyphenatedDate" name="reportDate" :outer-class="{ 'hidden-input': true }" />
    </div>

    <FormKit type="text" :modelValue="reference" name="reference" :outer-class="{ 'hidden-input': true }" />

    <!-- Currency used in the report -->
    <div class="form-field" data-test="currencyUsedInTheReport">
      <UploadFormHeader
        :name="euTaxonomyKpiNameMappings?.currency ?? 'Currency'"
        :explanation="euTaxonomyKpiInfoMappings?.currency ?? 'Currency'"
        :is-required="true"
      />
      <div class="lg:col-4 md:col-4 col-12 p-0">
        <FormKit
          type="text"
          name="currency"
          validation="required|length:2,3"
          validation-label="Currency used in the report"
          placeholder="Currency used in the report"
        />
      </div>
    </div>
    <!-- Integrated report is on a group level -->
    <div class="form-field">
      <RadioButtonsGroup
        :displayName="euTaxonomyKpiNameMappings?.groupLevelIntegratedReport ?? 'Group Level Integrated Report'"
        :info="euTaxonomyKpiInfoMappings?.groupLevelIntegratedReport ?? 'Group Level Integrated Report'"
        :name="'isGroupLevel'"
      />
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import RadioButtonsGroup from "@/components/forms/parts/RadioButtonsGroup.vue";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import Calendar from "primevue/calendar";
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";

export default defineComponent({
  name: "ReportFormElement",
  components: { FormKit, UploadFormHeader, RadioButtonsGroup, Calendar },
  data() {
    return {
      euTaxonomyKpiNameMappings,
      euTaxonomyKpiInfoMappings,
      reportDateAsDate: undefined as undefined | Date,
    };
  },
  mounted() {
    this.getDateFromString();
  },
  props: {
    name: {
      type: String,
      required: true,
    },
    reference: {
      type: String,
      required: true,
    },
    reportDate: {
      type: String,
      required: true,
    },
  },
  watch: {
    reportDate() {
      this.getDateFromString();
    }
  },
  computed: {
    hyphenatedDate() {
      if(this.reportDateAsDate) {
        return getHyphenatedDate(this.reportDateAsDate);
      }
      return "";
    }
  },
  emits: ["reportingDateChanged"],
  methods: {
    /**
     * computes an actual date object from the date string
     */
    getDateFromString() {
      this.reportDateAsDate = this.reportDate && this.reportDate.length > 1 ? new Date(this.reportDate) : undefined;
    },
    /**
     * Emits the event that the reporting date was changed
     *
     * @param newDate the new date
     */
    reportingDateChanged(newDate) {
      this.reportDateAsDate = newDate;
      this.$emit("reportingDateChanged", newDate);
    },
  },
});
</script>

<style scoped></style>
