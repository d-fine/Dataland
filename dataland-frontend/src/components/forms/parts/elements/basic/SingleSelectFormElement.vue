<template>
  <FormKit
    type="select"
    :validation-label="validationLabel"
    :validation="validation"
    :placeholder="placeholder"
    :name="name"
    :options="options"
    :inner-class="innerClass"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { SingleSelectFormElementProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "SingleSelectFormElement",
  components: { FormKit },
  props: { ...SingleSelectFormElementProps },
});
</script>
