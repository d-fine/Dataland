<template>
  <div class="container">
    <div class="row">
      <div class="col m6 s12">
        <FormKit v-model="data" type="form">
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
}

</script>