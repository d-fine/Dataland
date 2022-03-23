<template>
  <CardWrapper>
    <div class="card-title"><h2>Company Search</h2>
    </div>
    <div class="card-content ">
      <FormKit
          v-model="data"
          :submit-attrs="{
                  'name': 'getCompanies'
                }"
          submit-label="Search Company"
          type="form"
          @submit="getCompanyByName()">
        <FormKitSchema
            :data="data"
            :schema="schema"
        />

      </FormKit>
      <button class="btn btn-md orange darken-3" @click="getCompanyByName(true)">Show all companies</button>
      <br>
      <div class="col m12">
        <ResultTable v-if="response" :data="response.data" :headers="['Name', 'ID']" linkKey="companyName"
                     entity="Company Search" linkID="companyId" route="/companies/"/>
        <p v-else-if="response_error">The resource you requested does not exist yet. You can create it:
          <router-link to="/upload">Create Data</router-link>
        </p>
        <div>
        </div>
      </div>
    </div>
  </CardWrapper>
</template>

<script>
import {FormKit, FormKitSchema} from "@formkit/vue";
import {CompanyDataControllerApi} from "@/clients/backend";
import {DataStore} from "@/services/DataStore";
import backend from "@/clients/backend/backendOpenApi.json";

const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.PostCompanyRequestBody
const dataStore = new DataStore(api.getCompaniesByName, contactSchema)
import ResultTable from "@/components/ui/ResultTable";
import CardWrapper from "@/components/wrapper/CardWrapper";

export default {
  name: "RetrieveCompany",
  components: {CardWrapper, FormKit, FormKitSchema, ResultTable},

  data: () => ({
    data: {},
    schema: dataStore.getSchema(),
    model: {},
    response: null,
    response_error: false
  }),
  methods: {
    async getCompanyByName(all = false) {
      try {
        if (all) {
          this.data.companyName = ""
        }
        const inputArgs = Object.values(this.data)
        inputArgs.splice(0, 1)
        console.log(inputArgs)
        this.response = await dataStore.perform(...inputArgs)

      } catch (error) {
        console.error(error)
        this.response = null
        this.response_error = true
      }
    }
  },
}
</script>