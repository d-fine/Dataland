<template>
  <div class="card-content">
    <div class="card">
      <div class="card-title left"><h6>{{ title }}</h6></div>
      <div class="card-content">
        <p class="right-align green-text"><strong> {{ percentCalculation }} </strong> %</p>
        <div class="progress grey lighten-3">
          <div class="determinate green" :style='"width:" + percentCalculation + "%"'></div>
        </div>
        <h4 class="lar left-align" > € {{amount}} m</h4>
        <p class="left-align"><strong>Out of total of € {{ total }} m</strong></p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "TaxoCard",
  props: {
   title: {
     default: "Eligible Revenue",
     type: String},
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
    percentCalculation(){
      return (this.amount/this.total)*100
    }
  }
}
</script>