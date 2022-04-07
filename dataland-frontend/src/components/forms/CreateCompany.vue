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
      </FormKit>

        <template v-if="processed">
          <SuccessUpload v-if="response" msg="company" :count="count" :data="response.data" />
          <FailedUpload v-else msg="Company" :count="count" />
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
import Message from 'primevue/message';
const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.CompanyInformation
const dataStore = new DataStore(api.postCompany, contactSchema)

const createCompany = {
  name: "CreateCompany",
  components: {FailedUpload, Card, Message, FormKit, FormKitSchema, SuccessUpload},

  data: () => ({
    processed: false,
    model: {},
    schema: dataStore.getSchema(),
    response: null,
    count: 0
  }),
  methods: {
    async postCompanyData() {
      try {
        this.processed = false
        this.count++
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