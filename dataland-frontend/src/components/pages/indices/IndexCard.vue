<template>
  <Card class="bg-white d-card mr-2 cursor-pointer" >
    <template #index></template>
    <template #content>
      <div class="grid align-items-center">
        <div class="col-12 text-left pb-0">
        <strong>{{ index }}</strong>
        </div>
        <div class="col-5 text-left pt-0 text-gray-800">
          <p>Green asset ratio</p>
        </div>
        <div class="col-6 text-right text-green-500 pt-0">
          <span class="font-semibold text-xl">{{ green_asset_ratio }}</span>
          <span>%</span>
        </div>
      </div>
        <ProgressBar :value="green_asset_ratio" :showValue="false" class="bg-black-alpha-20 d-progressbar">
        </ProgressBar>
    </template>
  </Card>
</template>

<script>
import Card from "primevue/card";
import ProgressBar from 'primevue/progressbar';

export default {
  name: "IndexCard",
  components: {Card, ProgressBar},
  props: {
    index: {
      default: "DAX",
      type: String
    },
    green_asset_ratio: {
      default: 50,
      type: Number
    }
  }

}
</script>
<style>
.d-progressbar {
  height: 0.25rem;
  border-radius: 0.25rem;
}

.d-card {
  border-radius: 0.5rem;
  box-shadow: 0 0 32px 8px rgba(30, 30, 31, 0.08);
}

.d-card > .p-card-body {
  /*padding: 0.25rem 0.5rem 0.25rem 0.5rem;*/
}

.d-card > .p-card-body > .p-card-content {
  padding: 0;
}

</style>