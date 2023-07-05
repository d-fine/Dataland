<template>
  <Card class="bg-white d-card mr-2" data-test="taxocard">
    <template #title></template>
    <template #content>
      <div class="grid">
        <div class="col-6 text-left">
          <strong>{{ title }}</strong>
        </div>
        <div v-if="percent !== undefined && percent !== null" class="col-6 text-right text-primary">
          <span class="font-medium text-3xl" data-test="value">{{ percentCalculation }}</span>
          <span>%</span>
        </div>
        <div v-else-if="total == undefined && amount == undefined" class="col-6 grid align-items-center text-right">
          <span class="pl-4 font-semibold">No data has been reported </span>
        </div>
      </div>
      <PrimeProgressBar
        :value="percentCalculation"
        :showValue="false"
        class="bg-black-alpha-20 d-progressbar"
        v-if="percentCalculation !== undefined"
      />
      <div class="grid mt-4">
        <div class="col-12 text-left p-0 pl-2">
          <template v-if="amount !== undefined && amount !== null">
            <span class="font-medium text-3xl">€ </span>
            <span class="font-bold text-4xl">{{ valueWithOrderOfMagnitudeSuffix(amount) }}</span>
          </template>
          <p class="left-align" v-if="total !== undefined && total !== null">
            <strong>With a total of € {{ valueWithOrderOfMagnitudeSuffix(total) }}</strong>
          </p>
        </div>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import Card from "primevue/card";
import PrimeProgressBar from "primevue/progressbar";
import { convertCurrencyNumbersToNotationWithLetters } from "@/utils/CurrencyConverter";
import { defineComponent } from "vue";

export default defineComponent({
  name: "TaxoCard",
  components: { Card, PrimeProgressBar },
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
    amount: {
      type: Number,
    },
  },
  computed: {
    percentCalculation() {
      if (typeof this.percent === "number") {
        return Math.round(this.percent * 100 * 100) / 100;
      } else if (typeof this.amount === "number" && typeof this.total === "number" && this.total > 0) {
        return Math.round((this.amount / this.total) * 100 * 100) / 100;
      } else {
        return undefined;
      }
    },
  },
  methods: {
    /**
     * Transforms a number into a more readable format using abbreviations, e.g. 200132.12 -> 200.13 K
     * @param value the number to be transformed
     * @returns the string representation using abbreviations of the input value
     */
    valueWithOrderOfMagnitudeSuffix(value: number | undefined) {
      return convertCurrencyNumbersToNotationWithLetters(value, 2);
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
</style>
