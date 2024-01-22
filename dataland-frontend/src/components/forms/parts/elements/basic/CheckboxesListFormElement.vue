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
    v-if="!shouldBeIgnoredByFormKit"
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
      shouldBeIgnoredByFormKit: false,
      currentValue: null as string | null,
      checkboxValue: [] as Array<string>,
    };
  },
  methods: {
    disabledOnMoreThanOne,
    /**
     * Updates the currentValue when the checkboxes value has been changed
     * @param newCheckboxValue is the selected value in the checkbox that triggered this @input event
     */
    updateCurrentValue(newCheckboxValue: [string]) {
      const newCheckboxValueAsString = newCheckboxValue[0];
      if (newCheckboxValueAsString && newCheckboxValueAsString !== "") {
        this.shouldBeIgnoredByFormKit = false;
        this.currentValue = newCheckboxValueAsString;
        this.checkboxValue = newCheckboxValue;
      } else {
        this.shouldBeIgnoredByFormKit = !this.validation.includes("is:");
        this.currentValue = null;
      }
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
