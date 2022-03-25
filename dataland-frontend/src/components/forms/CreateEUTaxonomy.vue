<template>
  <Card class="col-5 col-offset-1">
    <template #title>Create EU Taxonomy Dataset
    </template>
    <template #content>
      <FormKit
        v-model="data"
        submit-label="Post EU-Taxonomy Dataset"
        :submit-attrs="{
                'name': 'postEUData'
              }"
        type="form"
        id="createEuTaxonomyForm"
        @submit="postEUData">
        <FormKit
            type="text"
            name="companyId"
            label="Company ID"
            @input="getCompanyIDs"
            :validation="[['required'],['is', ...idList]]"
            :validation-messages="{
                is: 'The company ID you provided does not exist.',
              }"
        />
        <FormKit
            type="group"
            name="dataSet"
            label="dataSet"
        >
          <FormKit
              type="select"
              name="Attestation"
              validation="required"
              label="Attestation"
              placeholder="Please choose"
              :options="
                    {'None':'None',
                    'Limited_Assurance': 'Limited Assurance',
                    'Reasonable_Assurance': 'Reasonable Assurance'}
                  "
          />
          <FormKit
              type="radio"
              name="Reporting Obligation"
              validation="required"
              label="Reporting Obligation"
              :outer-class="{
                      'formkit-outer': false,
                    }"
              :inner-class="{
                      'formkit-inner':false
                      }"
              :input-class="{
                      'formkit-input':false
                      }"

              :options="['Yes', 'No']"
          />
          <div title="capex">
            <h3>CapEx</h3>
            <FormKit
                type="group"
                name="Capex"
                label="CapEx"
            >
              <FormKit
                  type="text"
                  name="aligned"
                  validation="number"
                  label="Aligned / m€"
              />
              <FormKit
                  type="text"
                  name="eligible"
                  validation="number"
                  label="Eligible / m€"
              />
              <FormKit
                  type="text"
                  name="total"
                  validation="number"
                  label="Total / m€"
              />

            </FormKit>
          </div>
          <div title="opex">
            <h3>OpEx</h3>
            <FormKit
                type="group"
                name="Opex"
                label="OpEx"
            >
              <FormKit
                  type="text"
                  name="aligned"
                  validation="number"
                  label="Aligned / m€"
              />
              <FormKit
                  type="text"
                  name="eligible"
                  validation="number"
                  label="Eligible / m€"
              />
              <FormKit
                  type="text"
                  name="total"
                  validation="number"
                  label="Total / m€"
              />
            </FormKit>
          </div>
          <div title="revenue">
            <h3>Revenue</h3>
            <FormKit
                type="group"
                name="Revenue"
                label="Revenue"
            >
              <FormKit
                  type="text"
                  name="aligned"
                  validation="number"
                  label="Aligned / €"
              />
              <FormKit
                  type="text"
                  name="eligible"
                  validation="number"
                  label="Eligible / €"
              />
              <FormKit
                  type="text"
                  name="total"
                  validation="number"
                  label="Total / €"
              />
            </FormKit>
          </div>
        </FormKit>
      </FormKit>
      <template v-if="action">
        <SuccessUpload v-if="response" msg="company" :count="count"/>
        <FailedUpload v-else msg="Company" :count="count" />
      </template>

    </template>
  </Card>
</template>
<script>
import {EuTaxonomyDataControllerApi, CompanyDataControllerApi} from "@/clients/backend";
import SuccessUpload from "@/components/ui/SuccessUpload";
import {FormKit} from "@formkit/vue";
import {DataStore} from "@/services/DataStore";
import FailedUpload from "@/components/ui/FailedUpload";
import Card from 'primevue/card';
const api = new EuTaxonomyDataControllerApi()
const dataStore = new DataStore(api.postCompanyAssociatedDataSet)
const companyApi = new CompanyDataControllerApi()
const companyStore = new DataStore(companyApi.getCompaniesByName)
export default {
  name: "CustomEUTaxonomy",
  components: {FailedUpload, Card, FormKit, SuccessUpload},

  data: () => ({
    action: false,
    count: 0,
    data: {},
    model: {},
    loading: false,
    response: null,
    companyID: null,
    idList: []
  }),
  methods: {
    async getCompanyIDs(){
      try {
        const companyList = await companyStore.perform([""])
        this.idList = companyList.data.map(element => parseInt(Object.values(element)[1]))
      } catch(error) {
        this.idList = [0]
      }
    },

    async postEUData() {
      try {
        this.action = false
        this.count++
        this.response = await dataStore.perform(this.data)
        this.$formkit.reset('createEuTaxonomyForm')
      } catch (error) {
        this.response = null
        console.error(error)
      } finally {
        this.action = true
      }
    }
  },
}
</script>