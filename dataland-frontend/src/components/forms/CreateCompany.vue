<template>
  <CardWrapper>
    <div class="card-title"><h2>Create a Company</h2>
    </div>
    <div class="card-content ">
      <FormKit
          v-model="data"
          type="form"
          id="createCompanyForm"
          :submit-attrs="{
                  'name': 'postCompanyData'
                }"
          submit-label="Post Company"
          @submit="postCompanyData">
        <FormKitSchema
            :data="data"
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
            />
            <FormKit
                type="text"
                name="value"
                label="Identifier"
                placeholder="Identifier"
                validation="required"
            />
          </FormKit>
        </FormKit>
      </FormKit>
      <button @click="identifierListSize++"> Add a new identifier</button>
      <p> {{ data }}</p>
      <div class="progress" v-if="loading">
        <div class="indeterminate"></div>
      </div>
      <div v-if="enableClose" class="col m12">
        <div class="right-align">
          <button class="btn btn-small orange darken-3" @click="close">Close</button>
        </div>
        <SuccessUpload v-if="response" msg="company" :data="response.data" :status="response.status"
                       :enableClose="true"/>
        <FailedUpload v-if="errorOccurence" msg="Company" :enableClose="true"/>
      </div>
    </div>
  </CardWrapper>
</template>

<script>
import {FormKit, FormKitSchema} from "@formkit/vue";
import {CompanyDataControllerApi} from "@/../build/clients/backend";
import SuccessUpload from "@/components/ui/SuccessUpload";
import {DataStore} from "@/services/DataStore";
import backend from "@/../build/clients/backend/backendOpenApi.json";
import CardWrapper from "@/components/wrapper/CardWrapper";
import FailedUpload from "@/components/ui/FailedUpload";

const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.CompanyInformation
const dataStore = new DataStore(api.postCompany, contactSchema)

const createCompany = {
  name: "CreateCompany",
  components: {FailedUpload, CardWrapper, FormKit, FormKitSchema, SuccessUpload},

  data: () => ({
    enableClose: false,
    data: {},
    schema: dataStore.getSchema(),
    model: {},
    loading: false,
    response: null,
    errorOccurence: false,
    identifierListSize: 1
  }),
  created() {
    // delete auto identifiers
    delete this.schema[6]
  },
  methods: {
    close() {
      this.enableClose = false
    },
    async postCompanyData() {
      try {
        this.response = await dataStore.perform(this.data)
        this.$formkit.reset('createCompanyForm')
        this.errorOccurence = false
      } catch (error) {
        this.response = null
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