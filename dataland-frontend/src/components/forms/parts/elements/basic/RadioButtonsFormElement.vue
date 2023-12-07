<template>
  <FormKit
    type="checkbox"
    :key="key"
    name="name"
    v-model="checkboxValue"
    :options="options"
    :validation="validation"
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
    :ignore="shouldBeIgnored"
    :key="key"
    :outer-class="{ 'hidden-input': true, 'formkit-outer': false, }"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { disabledOnMoreThanOne } from "@/utils/FormKitPlugins";

export default defineComponent({
  name: "RadioButtonsFormElement",
  components: { FormKit },
  data() {
    return {
      key: 0,
      shouldBeIgnored: false,
      currentValue: null,
      checkboxValue: [],
    };
  },
  methods: {
    disabledOnMoreThanOne,
    /**
     * Emits an event when the currentValue has been changed
     * @param checkboxValue current value
     */
    emitUpdateCurrentValue(checkboxValue: [string]) {
      console.log("checkboxValue RadioButtonsFormElement.vue", checkboxValue);
      if (checkboxValue[0]) {
        this.shouldBeIgnored = false;
        this.key++;
        this.currentValue = checkboxValue[0].toString();
        this.$emit("update:currentValue", checkboxValue[0].toString());
      } else {
        this.shouldBeIgnored = true;
        this.key++;
        this.$emit("update:currentValue", null);
      }
    },
    setIgnoreToFields(newVal, oldVal) {
      console.log('Wewnetrzny 11 currentValue', newVal, oldVal)
      if (this.currentValue && this.currentValue !== "") {
        console.log('jeeeeeeee', this.currentValue)
        this.shouldBeIgnored = false;
        this.key++;
        this.checkboxValue = [this.currentValue];
        console.log('Wewnetrzny checkboxValue', this.checkboxValue)
      } else {
        this.shouldBeIgnored = true;
        this.key++;
      }
    },
  },
  watch: {
    currentValue(newVal) {
      this.setIgnoreToFields(newVal)
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
