<template>
  <TheHeader/>
  <TheContent>
  <div class="grid">
    <CreateCompany/>
    <CreateEUTaxonomy/>
  </div>
  </TheContent>
</template>

<script>
import CreateCompany from "@/components/forms/CreateCompany"
import CreateEUTaxonomy from "@/components/forms/CreateEUTaxonomy";
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";

export default {
  name: "CreateData",
  components: {
    TheContent,
    TheHeader,
    CreateEUTaxonomy,
    CreateCompany
  }
}
</script>

<style lang="scss">
@import "src/assets/css/variables";
</style>