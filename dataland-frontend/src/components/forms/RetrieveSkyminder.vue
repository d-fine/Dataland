<template>
  <Card class="col-12">
    <template #title>Skyminder Data Search
    </template>
    <template #content>
      <FormKit
          v-model="model"
          type="form"
          submit-label="Get Skyminder Data"
          :submit-attrs="{
                  'name': 'getSkyminderData'
                }"
          @submit="getSkyminderByName">
        <FormKit
            type="text"
            name="code"
            validation="required"
            label="messageCountry Code"
        />
        <FormKit
            type="text"
            name="name"
            validation="required"
            label="Company Name"
        />
      </FormKit>
      <br>
      <Button @click="clearAll" label="Clear"/>
      <div v-if="response" class="col m12">
        <SkyminderTable :headers="['Name', 'Address', 'Website', 'Email', 'Phone', 'Identifier']"
                        :data="response.data"/>
      </div>
    </template>
  </Card>
</template>

<script>
import {FormKit} from "@formkit/vue";
import {SkyminderControllerApi} from "@/../build/clients/backend/api";
import {ApiWrapper} from "@/services/ApiWrapper"
import Card from 'primevue/card';
import Button from 'primevue/button';
import SkyminderTable from "@/components/ui/SkyminderTable";

const skyminderControllerApi = new SkyminderControllerApi()
const getDataSkyminderRequestWrapper = new ApiWrapper(skyminderControllerApi.getDataSkyminderRequest)

export default {
  name: "RetrieveSkyminder",
  components: {Card, Button, FormKit, SkyminderTable},

  data: () => ({
    model: {},
    response: null
  }),
  methods: {
    clearAll() {
      this.model = {}
      this.response = null
    },

    async getSkyminderByName() {
      try {
        const inputArgs = Object.values(this.model)
        this.response = await getDataSkyminderRequestWrapper.perform(...inputArgs)
      } catch (error) {
        console.error(error)
      }
    }
  },

}

</script>

<style lang="css">
@import "../../assets/css/buttons.css";
@import "../../assets/css/forms.css";
</style>