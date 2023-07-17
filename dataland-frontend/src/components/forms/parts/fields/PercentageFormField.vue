<template>
  <div class="form-field">
    <UploadFormHeader :label="`${label} (%)`" :description="description" :is-required="required" />
    <FormKit
      type="text"
      :name="name"
      :validation-label="validationLabel ?? label"
      :validation="`number|between:0,100|${validation}`"
      :placeholder="placeholder ?? 'Value in %'"
      v-model="percentageFieldValue"
      :innerClass="innerClass"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { FormFieldPropsWithPlaceholder } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "PercentageFormField",
  components: { FormKit, UploadFormHeader },
  computed: {
    percentageFieldValue: {
      get(): string {
        return this.percentageFieldValueBind;
      },
      set(newValue: string) {
        this.$emit("update:percentageFieldValueBind", newValue);
      },
    },
  },
  props: {
    ...FormFieldPropsWithPlaceholder,
    percentageFieldValueBind: {
      type: [String, Number],
    },
  },
});
</script>
