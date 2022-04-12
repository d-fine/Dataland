<template>
  <MarginWrapper>
    <div class="grid align-items-center">
      <div class="col-8 text-left">
        <h1>Search EU Taxonomy data</h1>

      </div>
      <div class="col-4 text-right font-semibold">
        <span title="login message">Welcome to Dataland, Roger</span>
      </div>

    </div>
  </MarginWrapper>
    <EuTaxoSearchBar/>
</template>

<script>

import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "SearchTaxonomy",
  components: {MarginWrapper, EuTaxoSearchBar}
}
</script>