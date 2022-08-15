<template>
  <MarginWrapper>
    <div class="grid align-items-center">
      <div class="col-8 text-left">
        <h1 class="mb-0">Search Data for Companies</h1>
      </div>
      <div class="col-4 text-right font-semibold">
        <span title="login message">Welcome to Dataland</span>
      </div>
    </div>
  </MarginWrapper>
</template>
<script>
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "FrameworkDataSearchHeader",
  components: { MarginWrapper },
};
</script>
