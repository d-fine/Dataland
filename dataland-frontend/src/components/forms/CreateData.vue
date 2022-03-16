<template>
  <CreateCompany/>
  <CustomEUTaxonomy/>
</template>

<script>
import CreateCompany from "@/components/forms/CreateCompany"
import CustomEUTaxonomy from "@/components/forms/CustomEUTaxonomy";
export default {
  name: "CreateData",
  components: {
    CustomEUTaxonomy,
    CreateCompany
  }
}
</script>

<style scoped>

</style>