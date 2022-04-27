<template>
  <Card class="bg-white d-card mr-2">
    <template #title></template>
    <template #content>
      <div class="grid ">
        <div class="col-5 text-left">
          <strong>{{ title }}</strong>
        </div>
        <div v-if="amount" class="col-6 text-right text-green-500">
          <span class="font-semibold text-xl">{{ percentCalculation }}</span>
          <span>%</span>
        </div>
        <div v-else class="col-6 col-offset-1 grid align-items-center text-right">
          <i class="material-icons" aria-hidden="true"> error </i> <span
            class="pl-4 font-semibold">No data available</span>
        </div>
      </div>
      <ProgressBar :value="percentCalculation" :showValue="false" class="bg-black-alpha-20 d-progressbar">
      </ProgressBar>
      <div class="grid mt-4 ">
        <div class="col-6 text-left p-0 pl-2">
          <template v-if="amount">
            <span class="font-semibold text-lg">€ </span>
            <span class="font-bold text-2xl">{{ amount }}</span>
          </template>

          <p class="left-align"><strong>Out of total of € {{ orderOfMagnitudeSuffix }}</strong></p>
        </div>

      </div>
    </template>
  </Card>
</template>

<script>
import Card from "primevue/card";
import ProgressBar from 'primevue/progressbar';
import {numberFormatter} from "@/utils/currencyMagnitude";

export default {
  name: "TaxoCard",
  components: {Card, ProgressBar},
  props: {
    title: {
      default: "Eligible Revenue",
      type: String
    },
    total: {
      default: 1000,
      type: Number
    },
    percent: {
      default: 10,
      type: Number
    }

  },
  computed: {
    percentCalculation() {
      return Math.round(this.percent * 100 * 100) / 100
    },
    orderOfMagnitudeSuffix() {
      return numberFormatter(this.total, 2)
    },
    amount() {
      return numberFormatter(Math.round(this.total * this.percent * 100 * 100) / 100, 2)
    }
  },
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