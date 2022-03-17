<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div class="card">
          <div class="card-title"><h2>Skyminder Data Search</h2>
          </div>
          <div class="card-content ">
            <FormKit
                v-model="data"
                type="form"
                submit-label="Get Skyminder Data"
                :submit-attrs="{
                  'name': 'skyminder'
                }"
                @submit="getSkyminderByName">
              <FormKitSchema
                  :data="data"
                  :schema="schema"
              />
            </FormKit>
            <br>
            <div v-if="response" class="col m12">
              <SkyminderTable :headers="['Name', 'Address', 'Website', 'Email', 'Phone', 'Identifier']"
                              :data="response.data"/>
            </div>
            <button class="btn btn-sm orange darken-2" @click="clearAll">Clear</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {FormKit, FormKitSchema} from "@formkit/vue";
import {SkyminderControllerApi} from "@/clients/backend";
import {DataStore} from "@/services/DataStore";

const api = new SkyminderControllerApi()
const dataStore = new DataStore(api.getDataSkyminderRequest)
import SkyminderTable from "@/components/ui/SkyminderTable";

export default {
  name: "RetrieveSkyminder",
  components: {FormKitSchema, FormKit, SkyminderTable},

  data: () => ({
    data: {},
    schema: dataStore.getSchema(),
    model: {},
    response: null
  }),
  methods: {
    clearAll() {
      this.data = {}
    },

    async getSkyminderByName() {
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

<style lang="css">
@import "../../assets/css/buttons.css";
@import "../../assets/css/forms.css";
</style>