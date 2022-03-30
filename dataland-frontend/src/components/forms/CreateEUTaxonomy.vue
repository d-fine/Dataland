<template>
  <CardWrapper>
    <div class="card-title"><h2>Create EU Taxonomy Dataset</h2>
    </div>
    <div class="card-content ">

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
      <div class="progress" v-if="loading">
        <div class="indeterminate"></div>
      </div>
      <div v-if="enableClose" class="col m12">
        <div class="right-align">
          <button class="btn btn-small orange darken-3" @click="close">Close</button>
        </div>
        <SuccessUpload v-if="response" msg="EU Taxonomy Data" :data="{'DataId': response.data}" :status="response.status" :enableClose="true" />
        <FailedUpload v-if="errorOccurence" msg="EU Taxonomy Data" :enableClose="true" />
      </div>
    </div>
  </CardWrapper>
</template>
<script>
import {EuTaxonomyDataControllerApi, CompanyDataControllerApi} from "@/../build/clients/backend";
import SuccessUpload from "@/components/ui/SuccessUpload";
import {FormKit} from "@formkit/vue";
import CardWrapper from "@/components/wrapper/CardWrapper";
import {DataStore} from "@/services/DataStore";
import FailedUpload from "@/components/ui/FailedUpload";

const api = new EuTaxonomyDataControllerApi()
const dataStore = new DataStore(api.postCompanyAssociatedData)
const companyApi = new CompanyDataControllerApi()
const companyStore = new DataStore(companyApi.getCompaniesByName)
export default {
  name: "CustomEUTaxonomy",
  components: {FailedUpload, CardWrapper, FormKit, SuccessUpload},

  data: () => ({
    enableClose: false,
    errorOccurence: false,
    data: {},
    model: {},
    loading: false,
    response: null,
    companyID: null,
    idList: []
  }),
  methods: {
    close() {
      this.enableClose = false
    },
    async getCompanyIDs(){
      try {
        const companyList = await companyStore.perform([""])
        this.idList = Object.keys(companyList.data)
      } catch(error) {
        this.idList = [0]
      }
    },

    async postEUData() {
      try {
        this.response = await dataStore.perform(this.data)
        this.$formkit.reset('createEuTaxonomyForm')
        this.errorOccurence = false
      } catch (error) {
        this.response = null
        this.errorOccurence = true
        console.error(error)
      }
        this.enableClose = true
    }
  },
}
</script>