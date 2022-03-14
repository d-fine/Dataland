<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div class="card">
          <div class="card-title"><h2>Skyminder Data Search</h2>
          </div>
          <div class="card-content ">
            <FormKit v-model="data" type="form" @submit="getSkyminderByName">
              <FormKitSchema
                  :data="data"
                  :schema="schema"
              />
            </FormKit>
            <div class="progress" v-if="loading">
              <div class="indeterminate" ></div>
            </div>
            <div v-if="response" class="col m12">
              <ResultTable :headers="['Name', 'Address', 'Website', 'Email', 'Phone', 'Identifier']" :data="response.data"/>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "RetrieveCompany"
}
</script>

<style scoped>

</style>