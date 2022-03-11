<template>
  <div class="container">
    <div class="row">
      <div class="col m6 s12">
        <FormKit type="form" v-model="data" @submit="handleSubmit">
          <FormKitSchema
              :schema="schema"
              :data="data"
          />
        </FormKit>

      </div>
    </div>
  </div>
</template>

<script>

import {FormKitSchema, FormKit} from "@formkit/vue";
// import backend from "../schema/backendOpenApi.json"
import SchemaProcessor from "../services/SchemaProcessor"

// const properties = backend.components.schemas.CompanyMetaInformation.properties
const schemaProcessor = new SchemaProcessor()

const conditional_schema = {
  $formkit: 'text',
  if: '$companyId == "trigger"',
  label: "$companyId",
  placeholder: "$companyName",
  name: "hallo"
}


export default {
  data: () => ({
    data: {
      companyId: "",
      companyName: ""
    },
    schema: [
        schemaProcessor.process(),
      conditional_schema
    ]
    ,
    model: {

    }
  }),
  methods: {
    handleSubmit() {
      console.log(JSON.stringify(this.data, null, 2))
     }
  },
  components: {FormKitSchema, FormKit}
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