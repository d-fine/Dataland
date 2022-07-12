<template>
  <Card class="col-12">
    <template #title>Skyminder Data Search </template>
    <template #content>
      <FormKit
        v-model="model"
        type="form"
        submit-label="Get Skyminder Data"
        :submit-attrs="{
          name: 'getSkyminderData',
        }"
        @submit="getSkyminderByName"
      >
        <FormKit type="text" name="code" validation="required" label="Country Code" />
        <FormKit type="text" name="name" validation="required" label="Company Name" />
      </FormKit>
      <br />
      <PrimeButton @click="clearAll" label="Clear" />
      <div v-if="response" class="col m12">
        <SkyminderTable
          :headers="['Name', 'Address', 'Website', 'Email', 'Phone', 'Identifier']"
          :data="response.data"
        />
      </div>
    </template>
  </Card>
</template>

<script>
import { FormKit } from "@formkit/vue";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import { Button as PrimeButton } from "primevue/button";
import SkyminderTable from "@/components/tables/SkyminderTable";

export default {
  name: "RetrieveSkyminder",
  components: { Card, PrimeButton, FormKit, SkyminderTable },

  data: () => ({
    model: {},
    response: null,
  }),
  inject: ["getKeycloakInitPromise", "keycloak_init"],
  methods: {
    clearAll() {
      this.model = {};
      this.response = null;
    },

    async getSkyminderByName() {
      try {
        const inputArgs = Object.values(this.model);
        const skyminderControllerApi = await new ApiClientProvider(
          this.getKeycloakInitPromise(),
          this.keycloak_init
        ).getSkyminderControllerApi();
        this.response = await skyminderControllerApi.getDataSkyminderRequest(...inputArgs);
      } catch (error) {
        console.error(error);
      }
    },
  },
};
</script>
