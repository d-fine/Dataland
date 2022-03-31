<template>
  <Card class="bg-gray-400">
    <template #title></template>
    <template #content>
      <div class="grid">
        <div class="col col-6 text-left">
          <strong>{{ title }}</strong>
        </div>
        <div class="col col-6 text-right">
          <strong class="text-green-500 text-xl"> {{ percentCalculation }} %</strong>
        </div>
      </div>
      <ProgressBar :value="percentCalculation" :showValue="false" class="bg-black-alpha-20">
      </ProgressBar>
      <div class="grid">
        <div class="col col-6 text-left">
          <strong class="text-xl"> € {{ amount }} m</strong>
          <p class="left-align"><strong>Out of total of € {{ total }} m</strong></p>
        </div>
        <div class="col col-6 text-right">
        </div>
      </div>
    </template>
  </Card>
</template>

<script>
import Card from "primevue/card";
import ProgressBar from 'primevue/progressbar';

export default {
  name: "TaxoCard",
  components: {Card, ProgressBar},
  props: {
    title: {
      default: "Eligible Revenue",
      type: String
    },
    amount: {
      default: 100,
      type: Number
    },
    total: {
      default: 1000,
      type: Number
    }
    ,
    percent: {
      default: 10,
      type: Number
    }

  },
  computed: {
    percentCalculation() {
      return Math.round((this.amount / this.total) * 100 * 100) / 100
    }
  }
}
</script>