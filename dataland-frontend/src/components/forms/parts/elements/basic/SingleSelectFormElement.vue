<template>
  <FormKit
    type="select"
    :validation-label="validationLabel"
    :validation="validation"
    :placeholder="placeholder"
    :name="name"
    :options="options"
    outer-class="short"
  />
</template>

<script lang="ts">
import { type ComponentPropsOptions, defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "SingleSelectFormElement",
  components: { FormKit },
  props: { ...DropdownOptionFormFieldProps } as Readonly<ComponentPropsOptions>,
});
</script>
