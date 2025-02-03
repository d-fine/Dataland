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
// @ts-nocheck
import { FormKit } from '@formkit/vue';
import { defineComponent } from 'vue';

export default defineComponent({
  component: { FormKit },
  name: 'DataPointFormElement',
  data: () => ({
    inputValue: null,
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
    /**
     * This method is called by the visible FormKit element and used to transfer its state to the hidden
     * element used to ensure that the FormKit model matches the data model expected by the API
     */
    changed: function () {
      this.$refs.value.node.input(this.inputValue);
    },
  },
});
</script>
