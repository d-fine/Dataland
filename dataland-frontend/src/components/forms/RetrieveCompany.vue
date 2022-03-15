<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div class="card">
          <div class="card-title"><h2>Company Search</h2>
          </div>
          <div class="card-content ">
            <FormKit v-model="data" type="form" @submit="getCompanyByName">
              <FormKitSchema
                  :data="data"
                  :schema="schema"
              />
            </FormKit>
            <br>
            <div v-if="response" class="col m12">
              <ResultTable :headers="['Name', 'ID']" :data="response.data" entity="Company Search"/>
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
  }),
  methods: {
    async getCompanyByName() {
      try {
        const inputArgs = Object.values(this.data)
        inputArgs.splice(0, 1)
        this.response = await dataStore.perform(...inputArgs, {baseURL: process.env.VUE_APP_API_URL})
      } catch (error) {
        console.error(error)
      }
    }
  },
}
</script>

<style scoped>

</style>