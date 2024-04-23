<template>
  <div :class="containerClass">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <SingleSelectFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :placeholder="placeholder"
      :options="options"
      :input-class="inputClass"
      :is-required="required"
      :class="containerClass"
      @update:model-value="$emit('valueSelected', $event)"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { type ComponentPropsOptions, defineComponent } from "vue";
import SingleSelectFormElement from "@/components/forms/parts/elements/basic/SingleSelectFormElement.vue";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { deepCopyObject, type ObjectType } from "@/utils/UpdateObjectUtils";

export default defineComponent({
  name: "SingleSelectFormField",
  components: { SingleSelectFormElement, UploadFormHeader },
  props: Object.assign(deepCopyObject(DropdownOptionFormFieldProps as ObjectType), {
    inputClass: { type: String, default: "long" },
    containerClass: { type: String, default: "form-field" },
  }) as Readonly<ComponentPropsOptions>,
  emits: ["valueSelected"],
});
</script>
