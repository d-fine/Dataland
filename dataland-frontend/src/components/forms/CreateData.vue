<template>
  <div class="grid">
    <CreateCompany/>
    <CreateEUTaxonomy/>
  </div>
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

<style lang="scss">
@import "../../assets/css/variables";
</style>