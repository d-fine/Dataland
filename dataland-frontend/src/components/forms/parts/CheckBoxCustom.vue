<template>
  <div class="form-field-label">
    <FormKit
      ref="input"
      type="checkbox"
      :name="name"
      :value="false"
      validation="accepted"
      validation-label="Integrated report is on a group level"
      :outer-class="{
        'formkit-outer': false,
      }"
      :inner-class="{
        'formkit-inner': false,
      }"
      :input-class="{
        'formkit-input': false,
      }"
    />
    <h5>{{ name }}</h5>
    <em
      class="material-icons info-icon"
      aria-hidden="true"
      :title="name"
      v-tooltip.top="{
        value: explanation,
      }"
      >info</em
    >
  </div>
  <FormKitMessages :node="input?.node" />
</template>

<script lang="ts">
import Tooltip from "primevue/tooltip";
import { defineComponent, ref } from "vue";
import { FormKit } from "@formkit/vue";
import { FormKitMessages } from "@formkit/vue";

export default defineComponent({
  setup() {
    const input = ref();
    return {
      input,
    };
  },
  name: "CheckBoxCustom",
  directives: {
    tooltip: Tooltip,
  },
  components: { FormKit, FormKitMessages },
  props: {
    name: String,
    explanation: String,
  },
});
</script>
