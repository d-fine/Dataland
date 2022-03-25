<template>
  <Card class="col-5 col-offset-1">
    <template #title>Create a Company
    </template>
    <template #content>
      <FormKit
          v-model="data"
          type="form"
          id="createCompanyForm"
          :submit-attrs="{
                  'name': 'postCompanyData'
                }"
          submit-label="Post Company"
          @submit="postCompanyData">
        <FormKit
            type="text"
            name="companyName"
            validation="required"
            validation-visibility="submit"
            label="Company Name"
        />
      </FormKit>

        <template v-if="action">
          <SuccessUpload v-if="response" msg="company" :count="count"/>
          <FailedUpload v-else msg="Company" :count="count" />
        </template>
    </template>
  </Card>
</template>

<script>
import {FormKit} from "@formkit/vue";
import {CompanyDataControllerApi} from "@/clients/backend";
import SuccessUpload from "@/components/ui/SuccessUpload";
import {DataStore} from "@/services/DataStore";
import backend from "@/clients/backend/backendOpenApi.json";
import FailedUpload from "@/components/ui/FailedUpload";
import Card from 'primevue/card';
import Button from 'primevue/button';
import Message from 'primevue/message';
const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.PostCompanyRequestBody
const dataStore = new DataStore(api.postCompany, contactSchema)

const createCompany = {
  name: "CreateCompany",
  components: {FailedUpload, Card, Button, Message, FormKit, SuccessUpload},

  data: () => ({
    action: false,
    data: {},
    schema: dataStore.getSchema(),
    model: {},
    loading: false,
    response: null,
    count: 0
  }),
  methods: {
    close() {
      this.enableClose = false
    },
    async postCompanyData() {
      try {
        this.action = false
        this.count++
        this.response = await dataStore.perform(this.data)
        this.$formkit.reset('createCompanyForm')
      } catch (error) {
        this.response = null
      } finally {
        this.action = true
      }
    }
  },

}

export default createCompany

</script>

<style lang="scss">
@import "../../assets/css/forms.css";
</style>