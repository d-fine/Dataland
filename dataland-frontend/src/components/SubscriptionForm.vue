<template>
  <div class="container">
    <div class="row">
      <div class="col m6 s12">
        <FormKit type="form" v-model="data" @submit="handleSubmit" class="form">
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
import backend from "@/clients/backend/backendOpenApi.json"
import DataStore from "../services/DataStore"


import {CompanyDataControllerApi} from "@/clients/backend";

const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.ContactInformation
const dataStore = new DataStore(api.postCompany, contactSchema)

/*const conditional_schema = {
  $formkit: 'text',
  if: '$companyId"',
  label: "$companyId",
  placeholder: "$companyName",
  name: "hallo"
}*/


export default {
  data: () => ({
    data: {
    },
    schema: [
      dataStore.getSchema(),
      //conditional_schema
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