<template>
  <BigDecimalExtendedDataPointFormFieldDialog v-model:value="value"/>
  <PrimeButton label="SAVE CHANGES" icon="pi pi-save" style="margin-top: var(--spacing-md)" @click="updateDataPoint"/>
</template>

<script setup lang="ts">
import {inject, ref} from 'vue';
import PrimeButton from 'primevue/button';
import BigDecimalExtendedDataPointFormFieldDialog
  from "@/components/resources/dataTable/modals/BigDecimalExtendedDataPointFormFieldDialog.vue";
import {ApiClientProvider} from "@/services/ApiClients.ts";
import type Keycloak from "keycloak-js";
import {assertDefined} from "@/utils/TypeScriptUtils.ts";



const value = ref("");
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

async function updateDataPoint() {

  await apiClientProvider.apiClients.dataPointController.postDataPoint(
      {
        dataPoint: value.value,
        dataPointType: "extendedDecimalEstimatedMarketCapitalizationInEUR",
        companyId: "cbee1dd6-dd5f-469b-885a-ea9e7f28ed14",
        reportingPeriod: "2024"
      }
  ).catch((error) => {
    console.error(error);
  })

}
</script>
