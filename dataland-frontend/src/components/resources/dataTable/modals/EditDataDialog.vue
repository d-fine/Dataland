<template>
  <BigDecimalExtendedDataPointFormFieldDialog v-model:value="value"/>
  <PrimeButton label="SAVE CHANGES" icon="pi pi-save" style="margin-top: var(--spacing-md)" @click="updateDataPoint"/>
  <Message
      v-if="errorMessage"
      severity="error"
      variant="simple"
      size="small"
      data-test="reportingYearError"
  >
   {{errorMessage}}
  </Message>

</template>

<script setup lang="ts">
import {inject, onMounted, type Ref, ref, provide} from 'vue';
import PrimeButton from 'primevue/button';
import BigDecimalExtendedDataPointFormFieldDialog
  from "@/components/resources/dataTable/modals/BigDecimalExtendedDataPointFormFieldDialog.vue";
import {ApiClientProvider} from "@/services/ApiClients.ts";
import type Keycloak from "keycloak-js";
import {assertDefined} from "@/utils/TypeScriptUtils.ts";
import Message from "primevue/message";
import type {DynamicDialogInstance} from "primevue/dynamicdialogoptions";

const value = ref();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const errorMessage = ref('')
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
let companyID = ref<string | undefined>(undefined);


onMounted(() => {
  companyID.value = dialogRef?.value.data.companyID;
  provide('companyID', companyID.value);
});

async function updateDataPoint() {
  const dataPointPayload = JSON.stringify({ value: value.value });
  await apiClientProvider.apiClients.dataPointController.postDataPoint(
      {
        dataPoint: dataPointPayload,
        dataPointType: "extendedDecimalEstimatedMarketCapitalizationInEUR",
        companyId: companyID.value,
        reportingPeriod: "2024"
      },
      true
  ).catch((error) => {
    console.error(error);
    errorMessage.value = error.message
  })

}
</script>
