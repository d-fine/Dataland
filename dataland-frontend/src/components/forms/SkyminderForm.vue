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

import SchemaProcessor from "@/services/SchemaProcessor";

const dataStore = new SkyminderControllerApi()
const schemaProcessor = new SchemaProcessor(dataStore.getDataSkyminderRequest)

export default {
  name: "GetSkyminder",
  components: {FormKitSchema, FormKit},

  data: () => ({
    data: {},
    // ToDo: get scheme using classes
    schema: [schemaProcessor.getSchema()]
    ,
    model: {}
  }),
  methods: {
    async getSkyminderByName() {
      try {
        // ToDo: auto data.*
        this.response = await schemaProcessor.perform(this.data.name, this.data.code, {baseURL: process.env.VUE_APP_API_URL})
        // ToDO: Results Table
        console.log(this.response.data)
      } catch (error) {
        console.error(error)
      }
    }
  }
}

</script>