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
            :data="model"
            :schema="schema"
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
            <FormKit
                type="select"
                label="Identifier Type"
                name="type"
                placeholder="Please choose"
                :options="
                    {'Lei':'LEI',
                    'Isin': 'ISIN',
                    'PermId': 'PERM Id'}"
                validation="required"
                :inner-class="{
                                'formkit-inner':false,
                                'p-inputwrapper':true
                              }"
                :input-class="{
                                'formkit-input':false,
                                'p-inputtext': true
                              }"
            />
            <FormKit
                type="text"
                name="value"
                label="Identifier Value"
                placeholder="Identifier Value"
                validation="required"
                :inner-class="{
                                'formkit-inner':false,
                                'p-inputwrapper':true
                              }"
                :input-class="{
                                'formkit-input':false,
                                'p-inputtext': true
                              }"
            />
          </FormKit>
        </FormKit>
      </FormKit>
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
import {FormKit, FormKitSchema} from "@formkit/vue";
import {CompanyDataControllerApi} from "@/../build/clients/backend";
import SuccessUpload from "@/components/ui/SuccessUpload";
import {DataStore} from "@/services/DataStore";
import backend from "@/../build/clients/backend/backendOpenApi.json";
import FailedUpload from "@/components/ui/FailedUpload";
import Card from 'primevue/card';
import Button from "primevue/button";
import Message from 'primevue/message';
const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.CompanyInformation
const dataStore = new DataStore(api.postCompany, contactSchema)

const createCompany = {
  name: "CreateCompany",
  components: {FailedUpload, Card, Message, Button, FormKit, FormKitSchema, SuccessUpload},

  data: () => ({
    processed: false,
    model: {},
    schema: dataStore.getSchema(),
    response: null,
    messageCount: 0,
    identifierListSize: 1
  }),
  created() {
    // delete auto identifiers
    delete this.schema[6]
  },
  methods: {
    async postCompanyData() {
      try {
        this.processed = false
        this.messageCount++
        this.response = await dataStore.perform(this.model)
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

<style lang="scss">
@import "../../assets/css/forms.css";
</style>