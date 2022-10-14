<template>
  <Card class="col-12">
    <template #title>Skyminder Data Search </template>
    <template #content>
      <FormKit
        v-model="skyminderSearchParams"
        type="form"
        submit-label="Get Skyminder Data"
        :submit-attrs="{
          name: 'getSkyminderData',
        }"
        @submit="executeSkyminderSearch"
      >
        <FormKit type="text" name="code" validation="required" label="Country Code" />
        <FormKit type="text" name="name" validation="required" label="Company Name" />
      </FormKit>
      <br />
      <PrimeButton @click="clearSearch" label="Clear" />
      <div v-if="skyminderSearchResponse" class="col m12">
        <SkyminderTable
          :headers="['Name', 'Address', 'Website', 'Email', 'Phone', 'Identifier']"
          :data="skyminderSearchResponse.data"
        />
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import PrimeButton from "primevue/button";
import SkyminderTable from "@/components/resources/skyminderCompaniesSearch/SkyminderTable.vue";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";

export default defineComponent({
  name: "RetrieveSkyminder",
  components: { Card, PrimeButton, FormKit, SkyminderTable },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  data: () => ({
    skyminderSearchParams: {},
    skyminderSearchResponse: null,
  }),
  inject: ["getKeycloakPromise"],
  methods: {
    clearSearch(): void {
      this.skyminderSearchParams = {};
      this.skyminderSearchResponse = null;
    },

    async executeSkyminderSearch() {
      try {
        if (this.getKeycloakPromise !== undefined) {
          const inputArgs = Object.values(this.skyminderSearchParams);
          const skyminderControllerApi = await new ApiClientProvider(
            this.getKeycloakPromise()
          ).getSkyminderControllerApi();
          this.skyminderSearchResponse = await skyminderControllerApi.getDataSkyminderRequest(...inputArgs);
        }
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
