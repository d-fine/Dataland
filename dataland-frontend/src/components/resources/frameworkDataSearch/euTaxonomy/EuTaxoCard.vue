<template>
  <Card class="bg-white d-card mr-2">
    <template #title></template>
    <template #content>
      <div class="grid">
        <div class="col-7 text-left">
          <strong>{{ title }}</strong>
        </div>
        <div v-if="percent !== undefined && percent !== null" class="col-5 text-right text-primary">
          <span class="font-medium text-3xl">{{ percentCalculation }}</span>
          <span>%</span>
        </div>
        <div v-else class="col-5 col-offset-1 grid align-items-center text-right">
          <span class="pl-4 font-semibold">No data has been reported </span>
        </div>
      </div>
      <template v-if="percent !== undefined && percent !== null">
        <ProgressBar :value="percentCalculation" :showValue="false" class="bg-black-alpha-20 d-progressbar" />
        <div class="grid mt-4">
          <div class="col-12 text-left p-0 pl-2" v-if="total !== undefined && total !== null">
            <template v-if="amount !== undefined && amount !== null">
              <span class="font-medium text-3xl">€ </span>
              <span class="font-bold text-4xl">{{ amount }}</span>
            </template>
            <p class="left-align">
              <strong>Out of total of € {{ orderOfMagnitudeSuffix }}</strong>
            </p>
          </div>
        </div>
      </template>
    </template>
  </Card>
</template>

<script lang="ts">
import Card from "primevue/card";
import ProgressBar from "primevue/progressbar";
import { convertCurrencyNumbersToNotationWithLetters } from "@/utils/CurrencyConverter";
import { defineComponent } from "vue";

export default defineComponent({
  name: "TaxoCard",
  components: { Card, ProgressBar },
  props: {
    title: {
      type: String,
    },
    total: {
      type: Number,
    },
    percent: {
      type: Number,
    },
  },
  computed: {
    percentCalculation() {
      return Math.round(this.percent * 100 * 100) / 100;
    },
    orderOfMagnitudeSuffix() {
      return convertCurrencyNumbersToNotationWithLetters(this.total, 2);
    },
    amount() {
      return convertCurrencyNumbersToNotationWithLetters(Math.round(this.total * this.percent * 100) / 100, 2);
    },
  },
});
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

.d-card > .p-card-body > .p-card-content {
  padding: 0;
}
</style>
