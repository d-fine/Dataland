<template>
  <h4>Value</h4>
  <InputText placeholder="Insert Value" fluid v-model="value" @input="emitValue"/>
  <ExtendedDataPointFormFieldDialog
      :companyID="companyID"
      :dataPointProperties="dataPointProperties || undefined"
  />
</template>

<script setup lang="ts">

import { ref, inject } from "vue";
import InputText from "primevue/inputtext";
import ExtendedDataPointFormFieldDialog
  from "@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue";

const emit = defineEmits(['update:value'])
const value = ref("")

function emitValue() {
  emit('update:value', value.value)
}

const companyID = inject('companyID')

</script>

<style scoped>

</style>