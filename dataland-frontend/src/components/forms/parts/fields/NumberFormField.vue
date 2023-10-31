<template>
  <div class="mb-3">
    <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
    <div class="next-to-each-other">
      <FormKit
        type="text"
        :name="name"
        :unit="unit"
        v-model="currentValue"
        :validation-label="validationLabel ?? label"
        :validation="`number|${validation}`"
        :placeholder="unit ? `Value in ${unit}` : 'Value'"
        outer-class="short"
        :validationMessages="{ integer: `${validationLabel ?? label} must be an integer.` }"
        :validationRules="{ integer }"
        @blur="handleBlurValue"
      />
      <div class="form-field-label pb-3">
        <span>{{ unit }}</span>
      </div>
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
  },
  data() {
    return {
      currentValue: "",
    };
  },
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
  },
});
</script>
