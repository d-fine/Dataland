<template>
  <FormKit
    type="checkbox"
    :key="key"
    :name="name"
    v-model="checkboxValue"
    :options="options"
    :outer-class="{
      'yes-no-radio': true,
    }"
    :inner-class="{
      'formkit-inner': false,
    }"
    :input-class="{
      'formkit-input': false,
      'p-radiobutton': true,
    }"
    :id="fieldName + name"
    :ignore="true"
    :plugins="[disabledOnMoreThanOne]"
    @input="emitUpdateCurrentValue($event)"
  />
  <FormKit
    type="text"
    :name="name"
    v-model="currentValue"
    :validation="validation"
    :validation-label="validationLabel"
    v-if="!shouldBeIgnored"
    :outer-class="{ 'hidden-input': true, 'formkit-outer': false }"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { disabledOnMoreThanOne } from "@/utils/FormKitPlugins";

export default defineComponent({
  name: "CheckboxesListFormElement",
  components: { FormKit },
  data() {
    return {
      key: 0,
      shouldBeIgnored: false,
      currentValue: null,
      checkboxValue: [] as Array<string>,
    };
  },
  methods: {
    disabledOnMoreThanOne,
    /**
     * Emits an event when the currentValue has been changed
     * @param checkboxValue current value
     */
    emitUpdateCurrentValue(checkboxValue: [string]) {
      if (checkboxValue[0]) {
        this.shouldBeIgnored = false;
        this.currentValue = checkboxValue[0].toString();
        this.$emit("update:currentValue", checkboxValue[0].toString());
      } else {
        this.shouldBeIgnored = true;
        this.$emit("update:currentValue", null);
      }
    },
    /**
     * Function that sets whether value should be ignored or not
     * @param currentValue current value
     */
    setIgnoreToFields(currentValue: string) {
      if (currentValue && currentValue !== "") {
        this.shouldBeIgnored = false;
        this.checkboxValue = [currentValue];
      } else {
        this.shouldBeIgnored = true;
      }
    },
  },
  watch: {
    currentValue(newVal: string) {
      this.setIgnoreToFields(newVal);
    },
  },
  props: {
    name: {
      type: String,
      default: "",
    },
    options: {
      type: Object,
      required: true,
    },
    validation: {
      type: String,
      default: "",
    },
    validationLabel: {
      type: String,
      default: "",
    },
    fieldName: {
      type: String,
      default: "",
    },
  },
  emits: ["update:currentValue"],
});
</script>
