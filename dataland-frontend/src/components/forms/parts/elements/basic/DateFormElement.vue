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
    v-model="dateFormatted"
    outer-class="hidden-input"
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
      dateFormatted: undefined as string | undefined,
      date: undefined as Date | undefined,
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
