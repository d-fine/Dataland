<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div class="card">
          <div class="card-title"><h2>Company Search</h2>
          </div>
          <div class="card-content ">
            <FormKit v-model="data" type="form" @submit="getCompanyByName()">
              <FormKitSchema
                  :data="data"
                  :schema="schema"
              />
            </FormKit>
            <button class="btn btn-md orange darken-2" @click="getCompanyByName(true)">Show all companies</button>
            <br>
            <div class="col m12">
              <ResultTable v-if="response" :headers="['Name', 'ID', 'Link']" :data="response.data" entity="Company Search" route="/companies/" linkkey="companyId"/>
              <p v-else-if="response_error">The resource you requested does not exist yet. You can create it: <router-link to="/upload">Create Data</router-link></p>
            <div>
            </div>
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
const api = new CompanyDataControllerApi()
const dataStore = new DataStore(api.getCompaniesByName)
import ResultTable from "@/components/ui/ResultTable";
export default {
  name: "RetrieveCompany",
  components: {FormKitSchema, FormKit, ResultTable},

  data: () => ({
    data: {},
    schema: dataStore.getSchema(),
    model: {},
    response: null,
    response_error: false
  }),
  methods: {
    async getCompanyByName(all=false) {
      try {
        let inputArgs = Object.values(this.data)
        inputArgs.splice(0, 1)
        if (all) {
          inputArgs = [undefined]
        }
        this.response = await dataStore.perform(...inputArgs, {baseURL: process.env.VUE_APP_API_URL})

      } catch (error) {
        console.error(error)
        this.response = null
        this.response_error = true
      }
    }
  },
}
</script>

<style scoped>

</style>