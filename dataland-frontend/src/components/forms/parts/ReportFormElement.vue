<template>
  <FormKit :name="name" type="group">
    <!-- Date of the report -->
    <div class="form-field">
      <UploadFormHeader
        label="Report Date"
        description="The date until which the information presented in the report is valid."
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

    <FormKit type="text" :modelValue="fileReference" name="reference" :outer-class="{ 'hidden-input': true }" />

    <!-- Currency used in the report -->
    <SingleSelectFormField
      validation-label="Currency used in the report"
      placeholder="Currency used in the report"
      :options="countryCodeOptions"
      name="currency"
      label="Currency"
      description="The 3-letter alpha code that represents the currency used in the report."
    />
    <!-- Integrated report is on a group level -->
    <div class="form-field">
      <YesNoFormField name="isGroupLevel" description="Is the report on a group level?" label="Group Level Report" />
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import Calendar from "primevue/calendar";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import SingleSelectFormField from "@/components/forms/parts/fields/SingleSelectFormField.vue";

export default defineComponent({
  name: "ReportFormElement",
  components: { YesNoFormField, FormKit, UploadFormHeader, Calendar, SingleSelectFormField },
  data() {
    return {
      countryCodeOptions: getDataset(DropdownDatasetIdentifier.CurrencyCodes),
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
    fileReference: {
      type: String,
      required: true,
    },
    reportDate: {
      type: String,
    },
  },
  computed: {
    hyphenatedDate() {
      if (this.reportDateAsDate) {
        return getHyphenatedDate(this.reportDateAsDate);
      }
      return undefined;
    },
  },
  methods: {
    /**
     * computes an actual date object from the date string
     */
    getDateFromString() {
      this.reportDateAsDate = this.reportDate && this.reportDate.length > 1 ? new Date(this.reportDate) : undefined;
    },
    /**
     * Emits the event that the reporting date was changed
     * @param newDate the new date
     */
    reportingDateChanged(newDate: Date) {
      this.reportDateAsDate = newDate;
    },
  },
});
</script>
