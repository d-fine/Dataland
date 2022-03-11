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

import {getParams} from "@/utils/GetParameterNames"

const dataStore = new SkyminderControllerApi()
const contactSchema = getParams(dataStore.getDataSkyminderRequest)

export default {
  name: "APIClient",
  components: {FormKitSchema, FormKit},

  data: () => ({
    data: {},
    // ToDo: get scheme using classes
    schema: [
      {
        $formkit: 'text',
        for: ['item', 'key', contactSchema],
        label: "$item",
        placeholder: "$item",
        name: "$item"
      }
    ]
    ,
    model: {}
  }),
  methods: {
    async getSkyminderByName() {
      this.loading = true
      try {
        // ToDo: auto data.*
        this.response = await dataStore.getDataSkyminderRequest(this.data.name, this.data.code, {baseURL: process.env.VUE_APP_API_URL})
        // ToDO: Results Table
        console.log(this.response.data)
      } catch (error) {
        console.error(error)
      }
      this.loading = false
    }
  }
}

</script>