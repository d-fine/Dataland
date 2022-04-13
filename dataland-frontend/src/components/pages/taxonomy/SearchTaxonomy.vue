<template>
  <MarginWrapper>
    <div class="grid align-items-center">
      <div class="col-8 text-left">
        <h1 class="mb-0">Search EU Taxonomy data</h1>
      {{focus}}
      </div>
      <div class="col-4 text-right font-semibold">
        <span title="login message">Welcome to Dataland, Roger</span>
      </div>

    </div>
  </MarginWrapper>
    <EuTaxoSearchBar/>
  <MarginWrapper bgClass="surface-800">
    <IndexPanel/>
  </MarginWrapper>
</template>

<script>

import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import IndexPanel from "@/components/pages/indices/IndexPanel";

export default {
  name: "SearchTaxonomy",
  components: {IndexPanel, MarginWrapper, EuTaxoSearchBar}
}
</script>