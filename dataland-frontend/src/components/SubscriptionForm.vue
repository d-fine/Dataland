<template>
  <div class="container">
    <div class="row">
      <div class="col m6 s12">
        <card class="form">
          <FormKitSchema
              :schema="schema"
              :data="data"
          />
        </card>
      </div>
    </div>
  </div>
</template>

<script>

import {FormKitSchema} from "@formkit/vue";
import backend from "../schema/backendOpenApi.json"


export default {
  data: () => ({
    data: {properties: backend.components.schemas.CompanyMetaInformation.properties},
    schema: [{
      $formkit: 'text',
      for: ['item', 'key', "$properties"],
      label: "$key",
      placeholder: "$key"
    },
    ],
    model: {}
  }),
  methods: {
    submit() {
      console.log(JSON.stringify(this.model, null, 2))
    },
    reset() {
      this.$refs.formSchema.reset()
    }
  },
  components: {FormKitSchema}
}
</script>
<style>
.form {
  text-align: left;
  width: 600px;
  margin: auto;
}

h1 {
  font-size: 1.7em;
  text-align: center;
  margin-top: 0;
  margin-bottom: .2em
}

h1 + p {
  display: block;
  text-align: center;
  margin-bottom: 1.2em
}

small {
  line-height: 20px;
  display: block;
}

</style>