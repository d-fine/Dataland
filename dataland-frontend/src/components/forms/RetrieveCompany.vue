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
        <FormKit
            type="text"
            name="companyName"
            validation="required"
            validation-visibility="submit"
            label="Company Name"
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
import {FormKit} from "@formkit/vue";
import {CompanyDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";
import ResultTable from "@/components/ui/ResultTable";
import CardWrapper from "@/components/wrapper/CardWrapper";

const api = new CompanyDataControllerApi()
const dataStore = new DataStore(api.getCompaniesByName)

export default {
  name: "RetrieveCompany",
  components: {CardWrapper, FormKit, ResultTable},

  data: () => ({
    data: {},
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
        this.response = await dataStore.perform(this.data.companyName)

      } catch (error) {
        console.error(error)
        this.response = null
        this.response_error = true
      }
    }
  },
}
</script>