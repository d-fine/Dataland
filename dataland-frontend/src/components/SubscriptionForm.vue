<template>
  <card class="form">
    <FormSchema ref="formSchema" v-model="model" @submit.prevent
                :schema="schema.components.schemas.CompanyMetaInformation">
      <div>
        <button @click="submit">Subscribe</button>
        <button @click="reset">Reset</button>
      </div>
    </FormSchema>
  </card>
</template>

<script>
import { Vue3LiveForm } from  'vue3-live-form';

export default {
  data: () => ({
    schema: require('../clients/backend/backendOpenApi.json'),
    // schema: require('../schema/sample'),
    model: {}
  }),
  methods: {
    submit () {
      console.log(JSON.stringify(this.model, null, 2))
    },
    reset () {
      this.$refs.formSchema.reset()
    }
  },
  components: { Vue3LiveForm }
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