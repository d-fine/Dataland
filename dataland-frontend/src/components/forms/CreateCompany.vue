<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div class="card">
          <div class="card-title"><h2>Create a Company</h2>
          </div>
          <div class="card-content ">
            <FormKit
                v-model="data"
                type="form"
                :submit-attrs="{
                  'name': 'postCompanyData'
                }"
                submit-label="Post Company"
                @submit="postCompanyData">
              <FormKit
                  type="text"
                  name="companyName"
                  validation="required"
                  label="Company Name"
              />
            </FormKit>
            <div class="progress" v-if="loading">
              <div class="indeterminate"></div>
            </div>
            <div v-if="response" class="col m12">
              <SuccessUpload msg="company" :data="response.data" :status="response.status"/>
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
import SuccessUpload from "@/components/ui/SuccessUpload";
import {DataStore} from "@/services/DataStore";
import backend from "@/clients/backend/backendOpenApi.json";

const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.CompaniesRequestBody
const dataStore = new DataStore(api.postCompany, contactSchema)

const createCompany = {
  name: "CreateCompany",
  components: {FormKit, SuccessUpload, FormKitSchema},

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
      } catch (error) {
        console.error(error)
      }
    }
  },

}

export default createCompany

</script>

<style lang="scss">
@import "../../assets/css/forms.css";
</style>