<template>
  <MarginWrapper>
    <div class="grid align-items-center" v-if="!scrolled">
      <div class="col-8 text-left">
        <h1 class="mb-0">Search EU Taxonomy data</h1>
      </div>
      <div class="col-4 text-right font-semibold">
        <span title="login message">Welcome to Dataland</span>
      </div>
    </div>
  </MarginWrapper>
</template>
<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper"

export default {
  name: 'SearchTaxonomyHeader',
  components: {MarginWrapper},
  props: {
    scrolled: {
      type: Boolean,
      default: false
    }
  }
}
</script>