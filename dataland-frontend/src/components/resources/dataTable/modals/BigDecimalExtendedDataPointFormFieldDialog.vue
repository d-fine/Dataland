<template>
  <h4>Value</h4>
  <InputNumber placeholder="Insert Value" fluid v-model="value" @input="emitValue"/>
  <ExtendedDataPointFormFieldDialog
      :companyID="companyID!"
  />
</template>

<script setup lang="ts">

import { ref, inject } from "vue";
import InputNumber from "primevue/inputtext";
import ExtendedDataPointFormFieldDialog
  from "@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue";


const emit = defineEmits(['update:value'])
const value = ref<number | null>(null)

function emitValue() {
  emit('update:value', value.value)
}

const companyID = inject<string>('companyID');

</script>

<style scoped>

</style>