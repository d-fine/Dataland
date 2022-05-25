<template>
  <Card class="col-5 col-offset-1">
    <template #title>Create EU Taxonomy Dataset
    </template>
    <template #content>
      <FormKit
        v-model="model"
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
            placeholder="Company ID"
            @input="getCompanyIDs"
            :inner-class="innerClass"
            :input-class="inputClass"
            :validation="[['required'],['is', ...idList]]"
            :validation-messages="{
                is: 'The company ID you provided does not exist.',
              }"
        />
        <FormKit
            type="group"
            name="data"
            label="data"
        >
          <FormKit
              type="select"
              name="Attestation"
              validation="required"
              label="Attestation"
              placeholder="Please choose"
              :inner-class="innerClass"
              :input-class="inputClass"
              :options="
                    {'None':'None',
                    'LimitedAssurance': 'Limited Assurance',
                    'ReasonableAssurance': 'Reasonable Assurance'}
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
                      'formkit-input': false,
                      'p-radiobutton:': true
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
                  name="alignedPercentage"
                  validation="number"
                  label="Aligned / €"
                  :inner-class="innerClass"
                  :input-class="inputClass"
              />
              <FormKit
                  type="text"
                  name="eligiblePercentage"
                  validation="number"
                  label="Eligible / €"
                  :inner-class="innerClass"
                  :input-class="inputClass"
              />
              <FormKit
                  type="text"
                  name="totalAmount"
                  validation="number"
                  label="Total / €"
                  :inner-class="innerClass"
                  :input-class="inputClass"
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
                  name="alignedPercentage"
                  validation="number"
                  label="Aligned / €"
                  :inner-class="innerClass"
                  :input-class="inputClass"
              />
              <FormKit
                  type="text"
                  name="eligiblePercentage"
                  validation="number"
                  label="Eligible / €"
                  :inner-class="innerClass"
                  :input-class="inputClass"
              />
              <FormKit
                  type="text"
                  name="totalAmount"
                  validation="number"
                  label="Total / €"
                  :inner-class="innerClass"
                  :input-class="inputClass"
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
                  name="alignedPercentage"
                  validation="number"
                  label="Aligned / €"
                  :inner-class="innerClass"
                  :input-class="inputClass"
              />
              <FormKit
                  type="text"
                  name="eligiblePercentage"
                  validation="number"
                  label="Eligible / €"
                  :inner-class="innerClass"
                  :input-class="inputClass"
              />
              <FormKit
                  type="text"
                  name="totalAmount"
                  validation="number"
                  label="Total / €"
                  :inner-class="innerClass"
                  :input-class="inputClass"
              />
            </FormKit>
          </div>
        </FormKit>
      </FormKit>
      <template v-if="processed">
        <SuccessUpload v-if="response" msg="EU Taxonomy Data" :messageCount="messageCount" :data="response.data"/>
        <FailedUpload v-else msg="EU Taxonomy Data" :messageCount="messageCount" />
      </template>

    </template>
  </Card>
</template>
<script>
import SuccessUpload from "@/components/messages/SuccessUpload"
import {FormKit} from "@formkit/vue"
import FailedUpload from "@/components/messages/FailedUpload"
import Card from 'primevue/card'
import {getCompanyDataControllerApi, getEuTaxonomyDataControllerApi} from "@/services/ApiClients"

export default {
  name: "CustomEUTaxonomy",
  components: {FailedUpload, Card, FormKit, SuccessUpload},

  data: () => ({
    innerClass: {
      'formkit-inner': false,
      'p-inputwrapper': true
    },
    inputClass: {
      'formkit-input':false,
      'p-inputtext': true
    },
    processed: false,
    messageCount: 0,
    model: {},
    response: null,
    companyID: null,
    idList: []
  }),
  methods: {
    async getCompanyIDs(){
      try {
        const companyList = await getCompanyDataControllerApi.getCompanies("", "", true)
        this.idList = companyList.data.map(element => element.companyId)
      } catch(error) {
        this.idList = []
      }
    },

    async postEUData() {
      try {
        this.processed = false
        this.messageCount++
        this.response = await getEuTaxonomyDataControllerApi.postCompanyAssociatedData(this.model)
        this.$formkit.reset('createEuTaxonomyForm')
      } catch (error) {
        this.response = null
        console.error(error)
      } finally {
        this.processed = true
      }
    }
  },
}
</script>