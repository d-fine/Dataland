<template>
  <div class="container">
    <div class="row">
      <div class="col m6 s12">
        <FormKit v-model="data" type="form" @submit="getSkyminderByName">
          <FormKitSchema
              :data="data"
              :schema="schema"
          />
        </FormKit>
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

export default {
  name: "GetSkyminder",
  components: {FormKitSchema, FormKit},

  data: () => ({
    data: {},
    schema: dataStore.getSchema()
    ,
    model: {}
  }),
  methods: {
    async getSkyminderByName() {
      try {
        const inputArgs = Object.values(this.data)
        inputArgs.splice(0, 1)
        this.response = await dataStore.perform(...inputArgs, {baseURL: process.env.VUE_APP_API_URL})
        // ToDO: Results Table
        console.log(this.response.data)
      } catch (error) {
        console.error(error)
      }
    }
  }
}

</script>