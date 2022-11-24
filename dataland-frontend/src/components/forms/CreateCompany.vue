<template>
  <Card class="col-5 col-offset-1">
    <template #title>Create a Company </template>
    <template #content>
      <FormKit
        v-model="model"
        :actions="false"
        type="form"
        id="createCompanyForm"
        @submit="postCompanyData"
        #default="{ state: { valid } }"
      >
        <FormKitSchema :schema="companyInformationSchema" />
        <FormKit type="list" name="identifiers">
          <FormKit v-for="nIdentifier in identifierListSize" :key="nIdentifier" type="group">
            <FormKitSchema :schema="companyIdentifierSchema" />
          </FormKit>
        </FormKit>
        <FormKit type="list" name="companyAlternativeNames">
          <template v-for="nAlternativeNames in alternativeNamesListSize" :key="nAlternativeNames">
            <FormKit
              type="text"
              label="Alternative Name"
              placeholder="e.g. some Abbreviation"
              :inner-class="{
                'formkit-inner': false,
                'p-inputwrapper': true,
              }"
              :input-class="{
                'formkit-input': false,
                'p-inputtext': true,
              }"
            />
          </template>
        </FormKit>
        <FormKit type="submit" :disabled="!valid" label="Post Company" name="postCompanyData" />
      </FormKit>
      <p>{{ model }}</p>
      <PrimeButton v-if="alternativeNamesListSize < 1" @click="alternativeNamesListSize++" name="addAlternativeName">
        Add an alternative Name</PrimeButton
      >
      <PrimeButton v-if="alternativeNamesListSize >= 1" @click="alternativeNamesListSize++">
        Add another alternative Name</PrimeButton
      >
      <PrimeButton v-if="alternativeNamesListSize >= 1" @click="alternativeNamesListSize--" class="ml-2">
        Remove the last alternative Name
      </PrimeButton>
      <p></p>
      <PrimeButton @click="identifierListSize++"> Add a new identifier</PrimeButton>
      <PrimeButton v-if="identifierListSize > 1" @click="identifierListSize--" class="ml-2">
        Remove the last identifier
      </PrimeButton>
      <template v-if="postCompanyProcessed">
        <SuccessUpload
          v-if="postCompanyResponse"
          msg="company"
          :messageCount="messageCount"
          :data="postCompanyResponse.data"
        />
        <FailedUpload v-else msg="Company" :messageCount="messageCount" />
      </template>
    </template>
  </Card>
</template>

<script lang="ts">
import { FormKit, FormKitSchema } from "@formkit/vue";
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import { SchemaGenerator } from "@/services/SchemaGenerator";
import { ApiClientProvider } from "@/services/ApiClients";

import backend from "@projectRoot/dataland-backend/backendOpenApi.json";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import Card from "primevue/card";
import PrimeButton from "primevue/button";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { CompanyInformation } from "@clients/backend";
import { assertDefined } from "@/utils/TypeScriptUtils";

const companyInformation = backend.components.schemas.CompanyInformation;
const companyIdentifier = backend.components.schemas.CompanyIdentifier;
const companyInformationSchemaGenerator = new SchemaGenerator(companyInformation, ["isTeaserCompany"]);
const companyIdentifierSchemaGenerator = new SchemaGenerator(companyIdentifier);

export default defineComponent({
  name: "CreateCompany",
  components: {
    FailedUpload,
    Card,
    PrimeButton,
    FormKit,
    FormKitSchema,
    SuccessUpload,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  data: () => ({
    postCompanyProcessed: false,
    model: {} as CompanyInformation,
    companyInformationSchema: companyInformationSchemaGenerator.generate(),
    companyIdentifierSchema: companyIdentifierSchemaGenerator.generate(),
    postCompanyResponse: null,
    messageCount: 0,
    identifierListSize: 1,
    alternativeNamesListSize: 0,
  }),
  methods: {
    async postCompanyData() {
      try {
        this.postCompanyProcessed = false;
        this.messageCount++;
        const companyDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getCompanyDataControllerApi();
        this.postCompanyResponse = await companyDataControllerApi.postCompany(this.model);
        this.$formkit.reset("createCompanyForm");
      } catch (error) {
        console.error(error);
        this.postCompanyResponse = null;
      } finally {
        this.postCompanyProcessed = true;
      }
    },
  },
});
</script>
