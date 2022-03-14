<template>
  <CreateCompany/>
  <CreateEUTaxonomy/>
</template>

<script>
import CreateCompany from "@/components/forms/CreateCompany"
import CreateEUTaxonomy from "@/components/forms/CreateEUTaxonomy";
export default {
  name: "CreateData",
  components: {
    CreateEUTaxonomy,
    CreateCompany
  }
}
</script>

<style scoped>

</style>