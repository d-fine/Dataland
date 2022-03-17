<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div class="card">
          <div class="card-title"><h2>Create EU Taxonomy Dataset</h2>
          </div>
          <div class="card-content ">
            <FormKit
                type="text"
                name="companyID"
                validation="required|number"
                label="Company ID"
                v-model="companyID"

            />

            <FormKit
                v-model="data"
                :submit-attrs="{
                  'name': 'postEUData'
                }"
                type="form"
                @submit="postEUData">
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
                    name="opex"
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
                    name="revenue"
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
            <div class="progress" v-if="loading">
              <div class="indeterminate"></div>
            </div>
            <div v-if="response" class="col m12">
              <SuccessUpload msg="EU Taxonomy Data" :data="{'dataId': response.data}" :status="response.status"/>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import {EuTaxonomyDataControllerApi} from "@/clients/backend";
import SuccessUpload from "@/components/ui/SuccessUpload";
import {FormKit} from "@formkit/vue";

const api = new EuTaxonomyDataControllerApi()

export default {
  name: "CustomEUTaxonomy",
  components: {FormKit, SuccessUpload},

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
        this.response = await api.postData(this.companyID, this.data, {baseURL: process.env.VUE_APP_API_URL})
        console.log(this.response.status)
      } catch (error) {
        console.error(error)
      }
    }
  },
}
</script>

<style scoped lang="scss">
</style>