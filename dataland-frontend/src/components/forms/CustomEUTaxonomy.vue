<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div class="card">
          <div class="card-title"><h2>Create EU Taxonomy Dataset</h2>
          </div>
          <div class="card-content ">
            <FormKit v-model="data" type="form" @submit="postEUData">
              <FormKit
                  type="text"
                  name="companyID"
                  validation="required|number"
                  label="Company ID"
              />
              <FormKit
                  type="select"
                  name="Attestation"
                  validation="required"
                  label="Attestation"
                  placeholder = "Please choose"
                  :options="[
                  'Eligible',
                  'Super Mega',
                  'Not checked'
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

                  :options="{
                    true: 'yes',
                    false: 'no'
                  }"
              />
              <FormKit
                  type="text"
                  name="Capex"
                  validation="number"
                  label="Capex / €"
              />
              <FormKit
                  type="text"
                  name="Opex"
                  validation="number"
                  label="Opex / €"
              />
              <FormKit
                  type="text"
                  name="Revenue"
                  validation="number"
                  label="Revenue / €"
              />


            </FormKit>
            <div class="progress" v-if="loading">
              <div class="indeterminate"></div>
            </div>
            <div v-if="response" class="col m12">
              <SuccessUpload msg="company" :data="response.data" :status="response.status"/>
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
  }),
  mounted() {
    console.warn(this.data)
  },
  methods: {
    async postEUData() {
      try {
        this.response = await api.postData(this.data.companyID, this.data,{baseURL: process.env.VUE_APP_API_URL})
        console.log(this.response.status)
      } catch (error) {
        console.error(error)
      }
    }
  },
}
</script>

<style scoped lang="scss">
@import "src/assets/css/genesis/genesis";
</style>