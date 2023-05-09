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
    :validation-label="validationLabel"
    :validation="validation"
    :name="name"
    v-model="dateFormatted"
    outer-class="hidden-input"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import Calendar from "primevue/calendar";

export default defineComponent({
  name: "DateFormElement",
  components: { FormKit, Calendar },
  data() {
    return {
      dateFormatted: undefined as string | undefined,
      Date,
    };
  },
  watch: {
    date: {
      handler: function (date: Date | undefined) {
        if (date) {
          this.dateFormatted = getHyphenatedDate(date);
        } else {
          this.dateFormatted = undefined;
        }
      },
      immediate: true,
    },
  },
  computed: {
    date: {
      get(): Date | undefined {
        if (this.dateFormatted) {
          // Note: Appending the T00:00:00 ensures that the date is parsed to local-time 00:00:00 and not UTC 00:00:00
          return new Date(Date.parse(`${this.dateFormatted}T00:00:00`));
        } else {
          return undefined;
        }
      },
      set(newValue: Date | undefined): void {
        if (newValue) {
          this.dateFormatted = getHyphenatedDate(newValue);
        } else {
          this.dateFormatted = undefined;
        }
      },
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
    validationLabel: {
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
