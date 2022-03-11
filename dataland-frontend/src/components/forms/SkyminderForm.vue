<template>
  <div class="container">
    <div class="row">
      <div class="col s12 m12">
        <div class="card">
          <div class="card-title"><h2>Contact Data Search</h2>
          </div>
          <div class="card-content ">

            <FormKit type="form" v-model="data" @submit="handleSubmit" class="form">
              <FormKitSchema
                  :schema="schema"
                  :data="data"
              />
            </FormKit>

          </div>
        </div>
      </div>
      <div v-if="response" class="col m12">
        <ResultTable :headers="['Name', 'Address', 'Website', 'Email', 'Phone', 'Identifier']" :data="response.data"/>
      </div>

    </div>
  </div>
</template>

<script>
import {FormKitSchema, FormKit} from "@formkit/vue";
import backend from "schema/backendOpenApi.json"
import SchemaProcessor from "../services/SchemaProcessor"
const skyminderSchema = backend.components.schemas.ContactInformation
const schemaProcessor = new SchemaProcessor()

export default {
  name: "SkyminderForm",
  components: {Formkit, FormKitSchema}
}
</script>

<style scoped>

</style>