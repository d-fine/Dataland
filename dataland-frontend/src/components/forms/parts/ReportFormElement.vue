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
          data-test="publicationDate"
          inputId="icon"
          :modelValue="publicationDateAsDate"
          :showIcon="true"
          dateFormat="D, M dd, yy"
          @update:modelValue="reportingDateChanged($event)"
        />
      </div>
      <FormKit
        type="text"
        :modelValue="hyphenatedDate"
        name="publicationDate"
        :outer-class="{ 'hidden-input': true }"
      />
    </div>

    <FormKit type="text" :modelValue="fileReference" name="fileReference" :outer-class="{ 'hidden-input': true }" />
  </FormKit>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import Calendar from "primevue/calendar";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
export default defineComponent({
  name: "ReportFormElement",
  components: { FormKit, UploadFormHeader, Calendar },
  data() {
    return {
      publicationDateAsDate: undefined as undefined | Date,
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
    publicationDate: {
      type: String,
    },
  },
  computed: {
    hyphenatedDate() {
      if (this.publicationDateAsDate) {
        return getHyphenatedDate(this.publicationDateAsDate);
      }
      return undefined;
    },
  },
  methods: {
    /**
     * computes an actual date object from the date string
     */
    getDateFromString() {
      this.publicationDateAsDate =
        this.publicationDate && this.publicationDate.length > 1 ? new Date(this.publicationDate) : undefined;
    },
    /**
     * Emits the event that the reporting date was changed
     * @param newDate the new date
     */
    reportingDateChanged(newDate: Date) {
      this.publicationDateAsDate = newDate;
    },
  },
});
</script>
