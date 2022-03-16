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
                placeholder="Company ID"
                v-model="companyID"
            />
            <FormKit v-model="data" type="form" @submit="postEUData">
              <FormKitSchema
                  :data="data"
                  :schema="schema"
              />
            </FormKit>
            <div class="progress" v-if="loading">
              <div class="indeterminate" ></div>
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
import {FormKit, FormKitSchema} from "@formkit/vue";
import {EuTaxonomyDataControllerApi} from "@/clients/backend";
import SuccessUpload from "@/components/ui/SuccessUpload";
import {DataStore} from "@/services/DataStore";
import backend from "@/clients/backend/backendOpenApi.json";

const api = new EuTaxonomyDataControllerApi()
const contactSchema = backend.components.schemas.EuTaxonomyDataSet
const dataStore = new DataStore(api.postData, contactSchema)

export default {
  name: "CreateEUTaxonomy",
  components: {FormKitSchema, FormKit, SuccessUpload},

  data: () => ({
    data: {},
    schema: dataStore.getSchema(),
    model: {},
    loading: false,
    response: null,
    companyID: null
  }),
  methods: {
    async postEUData() {
      try {
        this.response = await dataStore.perform(this.companyID, this.data, {baseURL: process.env.VUE_APP_API_URL})
        console.log(this.response.status)
      } catch (error) {
        console.error(error)
      }
    }
  },

}

</script>

<style lang="scss">
@import "../../assets/css/forms.css";
@import "../../assets/css/genesis/genesis";
</style>