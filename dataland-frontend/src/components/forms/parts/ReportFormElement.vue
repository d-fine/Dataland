<template>
  <FormKit :name="name" type="group">
    <!-- Date of the report -->
    <div class="form-field">
      <UploadFormHeader
        name="Report Date"
        explanation="The date until which the information presented in the report is valid."
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
        name="Currency"
        explanation="The 3-letter alpha code that represents the currency used in the report."
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
        displayName="Group Level Integrated Report"
        info="Is the Integrated Report on a Group level?"
        name="isGroupLevel"
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
import { getHyphenatedDate } from "@/utils/DataFormatUtils";

export default defineComponent({
  name: "ReportFormElement",
  components: { FormKit, UploadFormHeader, RadioButtonsGroup, Calendar },
  data() {
    return {
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
