<template>
  <div class="grid align-items-center">
    <div class="col-8 text-left">
      <h1 class="pl-3">Search EU Taxonomy data</h1>

    </div>
    <div class="col-4 text-right font-semibold">
      <span class="pr-3">Welcome to Dataland, Roger</span>
    </div>

  </div>
  <EuTaxoSearchBar/>

</template>

<script>

import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";

export default {
  name: "SearchTaxonomy",
  components: {EuTaxoSearchBar}
}
</script>