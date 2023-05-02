<template>
  <MultiSelect
    v-model="selections"
    :options="options"
    :placeholder="placeholder"
    option-label="label"
    option-value="value"
    :show-toggle-all="false"
    :class="innerClass"
  />
  <FormKit
    type="list"
    :validation-label="displayName!"
    :validation="validation!"
    :name="name"
    v-model="selections"
    outer-class="hidden-input"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import MultiSelect from "primevue/multiselect";
import { DropdownOption } from "@/utils/PremadeDropdownDatasets";

export default defineComponent({
  name: "MultiSelectFormElement",
  components: { FormKit, MultiSelect },
  data() {
    return {
      selections: [] as string[],
    };
  },
  props: {
    name: {
      type: String,
      required: true,
    },
    displayName: {
      type: String,
      default: "",
    },
    validation: {
      type: String,
      default: "",
    },
    placeholder: {
      type: String,
      default: "",
    },
    options: {
      type: Array as () => Array<DropdownOption>,
      required: true,
    },
    innerClass: {
      type: String,
      default: "",
    },
  },
});
</script>
