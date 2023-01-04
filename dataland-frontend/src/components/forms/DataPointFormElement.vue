<template>
  <div :name="name">
    <FormKit
      v-model="inputValue"
      v-on:change="changed"
      type="text"
      :name="`${name}`"
      :label="label"
      validation="number"
      :ignore="true"
    />
    <FormKit type="group" :name="name" v-if="inputValue !== undefined && inputValue !== null && inputValue !== ''">
      <FormKit type="hidden" name="quality" value="Estimated" />
      <FormKit type="hidden" ref="value" name="value" :value="inputValue" validation="number" />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import { defineComponent } from "vue";

export default defineComponent({
  component: { FormKit },
  name: "DataPointFormElement",
  data: () => ({
    inputValue: null,
    innerClass: {
      "formkit-inner": false,
      "p-inputwrapper": true,
    },
    inputClass: {
      "formkit-input": false,
      "p-inputtext": true,
      "w-full": true,
    },
  }),
  props: {
    name: {
      type: String,
    },
    label: {
      type: String,
    },
  },
  methods: {
    changed: function () {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.$refs.value.node.input(this.inputValue);
    },
  },
});
</script>
