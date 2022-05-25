<template>
  <Card class="col-5 col-offset-1">
    <template #title>Create a Company
    </template>
    <template #content>
      <FormKit
          v-model="model"
          type="form"
          id="createCompanyForm"
          :submit-attrs="{
                  'name': 'postCompanyData'
                }"
          submit-label="Post Company"
          @submit="postCompanyData">
        <FormKitSchema
            :schema="companyInformationSchema"
        />
        <FormKit
            type="list"
            name="identifiers"
        >
          <FormKit
              v-for="nIdentifier in identifierListSize"
              :key="nIdentifier"
              type="group"
          >
            <FormKitSchema
                :schema="companyIdentifierSchema"
            />
          </FormKit>
        </FormKit>
      </FormKit>
      <p> {{ model }}</p>
      <Button @click="identifierListSize++"> Add a new identifier</Button>
      <Button v-if="identifierListSize>1" @click="identifierListSize--" class="ml-2"> Remove the last identifier</Button>
        <template v-if="processed">
          <SuccessUpload v-if="response" msg="company" :messageCount="messageCount" :data="response.data" />
          <FailedUpload v-else msg="Company" :messageCount="messageCount" />
        </template>
    </template>
  </Card>
</template>

<script>
import {FormKit, FormKitSchema} from "@formkit/vue"
import {CompanyDataControllerApi} from "@/../build/clients/backend/api"
import SuccessUpload from "@/components/messages/SuccessUpload"
import {SchemaGenerator} from "@/services/SchemaGenerator"
import {axiosDefaultConfiguration} from "@/services/AxiosDefaultConfiguration"
import backend from "@/../build/clients/backend/backendOpenApi.json"
import FailedUpload from "@/components/messages/FailedUpload"
import Card from 'primevue/card'
import Button from "primevue/button"
import Message from 'primevue/message'

const companyDataControllerApi = new CompanyDataControllerApi(axiosDefaultConfiguration)
const companyInformation = backend.components.schemas.CompanyInformation
const companyIdentifier = backend.components.schemas.CompanyIdentifier
const companyInformationSchemaGenerator = new SchemaGenerator(companyInformation)
const companyIdentifierSchemaGenerator = new SchemaGenerator(companyIdentifier)

const createCompany = {
  name: "CreateCompany",
  components: {FailedUpload, Card, Message, Button, FormKit, FormKitSchema, SuccessUpload},

  data: () => ({
    processed: false,
    model: {},
    companyInformationSchema: companyInformationSchemaGenerator.generate(),
    companyIdentifierSchema: companyIdentifierSchemaGenerator.generate(),
    response: null,
    messageCount: 0,
    identifierListSize: 1
  }),
  created() {
    // delete auto identifiers
    delete this.companyInformationSchema[6]
  },
  methods: {
    async postCompanyData() {
      try {
        this.processed = false
        this.messageCount++
        this.response = await companyDataControllerApi.postCompany(this.model)
        this.$formkit.reset('createCompanyForm')
      } catch (error) {
        console.error(error)
        this.response = null
      } finally {
        this.processed = true
      }
    }
  },

}

export default createCompany

</script>
