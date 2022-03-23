<template>
  <CardWrapper>
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
      <div v-if="enableClose" class="col m12">
        <div class="right-align">
        <button class="btn btn-small orange darken-3" @click="close">Close</button>
        </div>
        <SuccessUpload v-if="response" msg="company" :data="response.data" :status="response.status" :enableClose="true"/>
        <FailedUpload v-if="errorOccurence" msg="Company" :enableClose="true" />
      </div>
    </div>
  </CardWrapper>
</template>

<script>
import {FormKit} from "@formkit/vue";
import {CompanyDataControllerApi} from "@/clients/backend";
import SuccessUpload from "@/components/ui/SuccessUpload";
import {DataStore} from "@/services/DataStore";
import backend from "@/clients/backend/backendOpenApi.json";
import CardWrapper from "@/components/wrapper/CardWrapper";
import FailedUpload from "@/components/ui/FailedUpload";

const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.PostCompanyRequestBody
const dataStore = new DataStore(api.postCompany, contactSchema)

const createCompany = {
  name: "CreateCompany",
  components: {FailedUpload, CardWrapper, FormKit, SuccessUpload},

  data: () => ({
    enableClose: false,
    data: {},
    schema: dataStore.getSchema(),
    model: {},
    loading: false,
    response: null,
    errorOccurence: false
  }),
  methods: {
    close() {
      this.enableClose = false
    },
    async postCompanyData() {
      try {
        this.response = await dataStore.perform(this.data)
      } catch (error) {
        this.errorOccurence = true
      }
        this.enableClose = true
    }
  },

}

export default createCompany

</script>

<style lang="scss">
@import "../../assets/css/forms.css";
</style>