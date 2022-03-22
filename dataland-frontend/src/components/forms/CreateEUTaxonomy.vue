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
        @submit="postEUData">
        <FormKit
            type="text"
            name="companyId"
            validation="required|number"
            label="Company ID"
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
              :options="[
                    'None',
                    'Some',
                    'Full'
                  ]"
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
                  name="aligned_turnover"
                  validation="number"
                  label="Aligned Turnover / €"
              />
              <FormKit
                  type="text"
                  name="eligible_turnover"
                  validation="number"
                  label="Eligible Turnover / €"
              />
              <FormKit
                  type="text"
                  name="total"
                  validation="number"
                  label="Total / €"
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
                  name="aligned_turnover"
                  validation="number"
                  label="Aligned Turnover / €"
              />
              <FormKit
                  type="text"
                  name="eligible_turnover"
                  validation="number"
                  label="Eligible Turnover / €"
              />
              <FormKit
                  type="text"
                  name="total"
                  validation="number"
                  label="Total / €"
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
                  name="aligned_turnover"
                  validation="number"
                  label="Aligned Turnover / €"
              />
              <FormKit
                  type="text"
                  name="eligible_turnover"
                  validation="number"
                  label="Eligible Turnover / €"
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
      <p>{{data}}</p>
      <div class="progress" v-if="loading">
        <div class="indeterminate"></div>
      </div>
      <div v-if="response" class="col m12">
        <SuccessUpload msg="EU Taxonomy Data" :data="{'dataId': response.data}" :status="response.status"/>
      </div>
    </div>
  </CardWrapper>
</template>
<script>
import {EuTaxonomyDataControllerApi} from "@/clients/backend";
import SuccessUpload from "@/components/ui/SuccessUpload";
import {FormKit} from "@formkit/vue";
import CardWrapper from "@/components/wrapper/CardWrapper";

const api = new EuTaxonomyDataControllerApi()

export default {
  name: "CustomEUTaxonomy",
  components: {CardWrapper, FormKit, SuccessUpload},

  data: () => ({
    data: {},
    model: {},
    loading: false,
    response: null,
    companyID: null
  }),
  methods: {
    async postEUData() {
      try {
        this.response = await api.postData(this.data, {baseURL: process.env.VUE_APP_BASE_API_URL})
        console.log(this.response.status)
      } catch (error) {
        console.error(error)
      }
    }
  },
}
</script>