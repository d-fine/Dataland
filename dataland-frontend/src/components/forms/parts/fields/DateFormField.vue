<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" :is-required="required" />
    <DateFormElement
      :name="name"
      :display-name="displayName"
      :validation="validation"
      :placeholder="placeholder"
      :today-as-max="todayAsMax"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import DateFormElement from "@/components/forms/parts/elements/basic/DateFormElement.vue";

export default defineComponent({
  name: "DateFormField",
  components: { UploadFormHeader, DateFormElement },
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
  },
  props: {
    name: {
      type: String,
      required: true,
    },
    info: {
      type: String,
      default: "",
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
    required: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
