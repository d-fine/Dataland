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
    @input="updateCurrentValue($event)"
  />
  <FormKit
    type="text"
    :name="name"
    v-model="currentValue"
    :validation="validation"
    :validation-label="validationLabel"
    :validation-messages="validationMessages"
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
     * Updates the currentValue when the checkboxes value has been changed
     * @param checkboxValue current selection in the checkbox
     */
    updateCurrentValue(checkboxValue: [string]) {
      if (checkboxValue[0]) {
        this.shouldBeIgnored = false;
        this.currentValue = checkboxValue[0].toString();
      } else {
        this.currentValue = null;
        this.shouldBeIgnored = !this.validation.includes("is:");
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
        this.shouldBeIgnored = !this.validation.includes("is:");
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
    validationMessages: {
      type: Object as () => { is: string },
    },
    fieldName: {
      type: String,
      default: "",
    },
  },
});
</script>
