<template>
  <CardWrapper>
    <div class="card-title"><h2>Skyminder Data Search</h2>
    </div>
    <div class="card-content ">
      <FormKit
          v-model="data"
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
            label="Country Code"
        />
        <FormKit
            type="text"
            name="name"
            validation="required"
            label="Company Name"
        />
      </FormKit>
      <br>
      <div v-if="response" class="col m12">
        <SkyminderTable :headers="['Name', 'Address', 'Website', 'Email', 'Phone', 'Identifier']"
                        :data="response.data"/>
      </div>
      <button class="btn btn-sm orange darken-3" @click="clearAll">Clear</button>
    </div>
  </CardWrapper>
</template>

<script>
import {FormKit} from "@formkit/vue";
import {SkyminderControllerApi} from "@/clients/backend";
import {DataStore} from "@/services/DataStore";

const api = new SkyminderControllerApi()
const dataStore = new DataStore(api.getDataSkyminderRequest)
import SkyminderTable from "@/components/ui/SkyminderTable";
import CardWrapper from "@/components/wrapper/CardWrapper";

export default {
  name: "RetrieveSkyminder",
  components: {CardWrapper, FormKit, SkyminderTable},

  data: () => ({
    data: {},
    schema: dataStore.getSchema(),
    model: {},
    response: null
  }),
  methods: {
    clearAll() {
      this.data = {}
    },

    async getSkyminderByName() {
        const inputArgs = Object.values(this.data)
        this.response = await dataStore.perform(...inputArgs)
    }
  },

}

</script>

<style lang="css">
@import "../../assets/css/buttons.css";
@import "../../assets/css/forms.css";
</style>