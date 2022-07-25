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
        <FormKit type="submit" :disabled="!valid" label="Post Company" name="postCompanyData" />
      </FormKit>
      <p>{{ model }}</p>
      <Button @click="identifierListSize++"> Add a new identifier</Button>
      <Button v-if="identifierListSize > 1" @click="identifierListSize--" class="ml-2">
        Remove the last identifier
      </Button>
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

<script>
import { FormKit, FormKitSchema } from "@formkit/vue";
import SuccessUpload from "@/components/messages/SuccessUpload";
import { SchemaGenerator } from "@/services/SchemaGenerator";
import { ApiClientProvider } from "@/services/ApiClients";
import backend from "@/../build/clients/backend/backendOpenApi.json";
import FailedUpload from "@/components/messages/FailedUpload";
import Card from "primevue/card";
import Button from "primevue/button";
import Message from "primevue/message";

const companyInformation = backend.components.schemas.CompanyInformation;
const companyIdentifier = backend.components.schemas.CompanyIdentifier;
const companyInformationSchemaGenerator = new SchemaGenerator(companyInformation);
const companyIdentifierSchemaGenerator = new SchemaGenerator(companyIdentifier);

const createCompany = {
  name: "CreateCompany",
  components: { FailedUpload, Card, Message, Button, FormKit, FormKitSchema, SuccessUpload },

  data: () => ({
    postCompanyProcessed: false,
    model: {},
    companyInformationSchema: companyInformationSchemaGenerator.generate(),
    companyIdentifierSchema: companyIdentifierSchemaGenerator.generate(),
    postCompanyResponse: null,
    messageCount: 0,
    identifierListSize: 1,
  }),
  inject: ["getKeycloakInitPromise", "keycloak_init"],
  methods: {
    async postCompanyData() {
      try {
        this.postCompanyProcessed = false;
        this.messageCount++;
        const companyDataControllerApi = await new ApiClientProvider(
          this.getKeycloakInitPromise(),
          this.keycloak_init
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
};

export default createCompany;
</script>
