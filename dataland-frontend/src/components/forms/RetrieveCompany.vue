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
        <table v-if="response">
          <caption><h4>Company Search</h4></caption>
          <thead>
          <tr>
            <th v-for="(header, i) in ['Name', 'Headquarter', 'Sector','Market Cap', 'Market Cap Date']" :key="i">
              {{ header }}
            </th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(dataset, index) in response.data" :key="index">
            <td v-for="(item, i) in dataset.companyInformation" :key="i">
              <router-link v-if="i === 'companyName'" :to="'/companies/'+ dataset.companyId ">{{ item }}</router-link>
              <template v-else>
                {{ item }}
              </template>
            </td>
          </tr>
          </tbody>
        </table>
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
import CardWrapper from "@/components/wrapper/CardWrapper";

const api = new CompanyDataControllerApi()
const dataStore = new DataStore(api.getCompanies)

export default {
  name: "RetrieveCompany",
  components: {CardWrapper, FormKit},

  data: () => ({
    data: {},
    model: {},
    response: null,
    companyInformation: null,
    response_error: false
  }),
  methods: {
    async getCompanyByName(all = false) {
      try {
        if (all) {
          this.data.companyName = ""
        }
        this.response = await dataStore.perform(this.data.companyName, "", true)
      } catch (error) {
        console.error(error)
        this.response = null
        this.response_error = true
      }
    }
  },
}
</script>