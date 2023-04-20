<template>
  <div class="lg:col-4 md:col-6 col-12 pl-0">
    <Calendar
      inputId="icon"
      v-model="date"
      :showIcon="true"
      dateFormat="D, M dd, yy"
      :maxDate="todayAsMax ? new Date() : undefined"
      :placeholder="placeholder"
    />
  </div>

  <FormKit
    type="text"
    :validation-label="displayName!"
    :validation="validation!"
    :name="name"
    v-model="hyphenatedDate"
    outer-class="hidden-input"
    :ignore="!isRequired && hyphenatedDate.length == 0"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import Calendar from "primevue/calendar";
import { isInputRequired } from "@/utils/validationsUtils";

export default defineComponent({
  name: "DateFormElement",
  components: { FormKit, Calendar },
  data() {
    return {
      date: undefined as Date | undefined,
      Date,
    };
  },
  computed: {
    hyphenatedDate(): string {
      if (this.date) {
        return getHyphenatedDate(this.date);
      } else {
        return "";
      }
    },
    isRequired(): boolean {
      return isInputRequired(this.validation);
    },
  },
  props: {
    name: {
      type: String,
      required: true,
    },
    displayName: {
      type: String,
      default: "",
    },
    validation: {
      type: String,
      default: "",
    },
    placeholder: {
      type: String,
      default: "",
    },
    todayAsMax: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
