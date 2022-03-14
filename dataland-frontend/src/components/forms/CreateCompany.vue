<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div class="card">
          <div class="card-title"><h2>Create A Company</h2>
          </div>
          <div class="card-content ">
            <FormKit v-model="data" type="form" @submit="postCompanyData">
              <FormKitSchema
                  :data="data"
                  :schema="schema"
              />
            </FormKit>
            <div class="progress" v-if="loading">
              <div class="indeterminate" ></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {FormKit, FormKitSchema} from "@formkit/vue";
import {CompanyDataControllerApi} from "@/clients/backend";

import {DataStore} from "@/services/DataStore";
import backend from "@/clients/backend/backendOpenApi.json";

const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.ContactInformation
const dataStore = new DataStore(api.postCompany, contactSchema)

export default {
  name: "CreateCompany",
  components: {FormKitSchema, FormKit},

  data: () => ({
    data: {},
    schema: dataStore.getSchema(),
    model: {},
    loading: false,
    response: null,
  }),
  methods: {
    async postCompanyData() {
      try {
        this.response = await dataStore.perform(this.data, {baseURL: process.env.VUE_APP_API_URL})
        console.log(this.response)
      } catch (error) {
        console.error(error)
      }
    }
  },

}

</script>

<style lang="scss">
@import "../../assets/css/forms.css";
@import "../../assets/css/variables";
@import "../../assets/css/button-and-submit";
</style>