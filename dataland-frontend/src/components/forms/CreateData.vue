<template>
  <Knob v-model="value"/>
  <CreateCompany/>
  <CreateEUTaxonomy/>
</template>

<script>
import CreateCompany from "@/components/forms/CreateCompany"
import CreateEUTaxonomy from "@/components/forms/CreateEUTaxonomy";
import Knob from 'primevue/knob';
export default {
  name: "CreateData",
  components: {
    CreateEUTaxonomy,
    CreateCompany,
    Knob
  },
  data(){
    return {
      value: 5
    }
  }
}
</script>

<style lang="scss">
@import "../../assets/css/variables";
</style>