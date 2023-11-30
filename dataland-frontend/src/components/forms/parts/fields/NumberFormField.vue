<template>
  <UploadFormHeader v-if="label" :label="label" :description="description" :is-required="required" />
  <div class="grid" :data-test="name">
    <FormKit
      type="text"
      :name="name"
      :unit="unit"
      :value="currentValue"
      :validation-label="validationLabel ?? label"
      :validation="`number|${validation}`"
      :placeholder="unit ? `Value in ${unit}` : 'Value'"
      :validationMessages="{ integer: `${validationLabel ?? label} must be an integer.` }"
      :validationRules="{ integer }"
      :outer-class="inputClass"
      @input="$emit('update:currentValue', $event)"
    />
    <div v-if="unit" class="form-field-label pb-4 col-4">
      <span>{{ unit }}</span>
    </div>
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { FormFieldPropsWithPlaceholder } from "@/components/forms/parts/fields/FormFieldProps";
import { type FormKitNode } from "@formkit/core";

export default defineComponent({
  name: "NumberFormField",

  components: { FormKit, UploadFormHeader },
  props: {
    ...FormFieldPropsWithPlaceholder,
    unit: String,
    currentValue: String,
  },
  watch: {
    currentValue() {
      this.emitUpdateCurrentValue();
    },
  },
  emits: ["update:currentValue"],

  methods: {
    /**
     * Checks if a node has an integer value
     * @param node Node whose value to check for being an integer
     * @returns true iff the provided node value is an integer
     */
    integer(node: FormKitNode): boolean {
      const fieldValue = node.value as string;
      return !isNaN(parseInt(fieldValue)) && parseInt(fieldValue) == parseFloat(fieldValue);
    },
    /**
     * Emits an event when the currentValue has been changed
     */
    emitUpdateCurrentValue() {
      this.$emit("update:currentValue", this.currentValue);
    },
  },
});
</script>
