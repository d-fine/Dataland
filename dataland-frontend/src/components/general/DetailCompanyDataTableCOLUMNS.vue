<template>
  <DataTable :value="sales" responsiveLayout="scroll">
    <template #header>
      <div>
        <Button icon="pi pi-refresh" style="float: left"/>
        List of Cars
      </div>
    </template>
    <ColumnGroup type="header">
      <Row>
        <Column header="KPIs" rowEditor />
        <Column header="Sale Rate" :colspan="4" />
      </Row>
      <Row>
        <Column header="Sales" :colspan="2" />
        <Column header="Profits" :colspan="2" />
      </Row>
      <Row>
        <Column header="Last Year" :sortable="true" field="lastYearSale" rowEditor  />
        <Column header="This Year" :sortable="true" field="thisYearSale" rowEditor  />
        <Column header="Last Year" :sortable="true" field="lastYearProfit" rowEditor  />
        <Column header="This Year" :sortable="true" field="thisYearProfit" rowEditor  />
      </Row>
    </ColumnGroup>
    <Column field="product" />
    <Column field="lastYearSale">
      <template #body="slotProps"> {{ slotProps.data.lastYearSale }}% </template>
    </Column>
    <Column field="thisYearSale">
      <template #body="slotProps"> {{ slotProps.data.thisYearSale }}% </template>
    </Column>
    <Column field="lastYearProfit">
      <template #body="slotProps">
        {{ formatCurrency(slotProps.data.lastYearProfit) }}
      </template>
    </Column>
    <Column field="thisYearProfit">
      <template #body="slotProps">
        {{ formatCurrency(slotProps.data.thisYearProfit) }}
      </template>
    </Column>
    <ColumnGroup type="footer">
      <Row>
        <Column footer="Totals:" :colspan="3" footerStyle="text-align:right" />
        <Column :footer="lastYearTotal" />
        <Column :footer="thisYearTotal" />
      </Row>
    </ColumnGroup>
  </DataTable>
</template>

<script>
import { defineComponent } from "vue";
import DataTable from "primevue/datatable";
import ColumnGroup from "primevue/columngroup";
import Column from "primevue/column";
import Row from "primevue/row";

export default defineComponent({
  name: "DetailCompanyDataTableCOLUMNS",
  components: { DataTable, Column, Row, ColumnGroup },
  data() {
    return {
      sales: null,
      expandedRowGroups: null,
    };
  },
  props: {
    dataSet: {
      type: [],
      default: [],
    },
  },
  created() {
    this.sales = [
      { product: "Bamboo Watch", lastYearSale: 51, thisYearSale: 40, lastYearProfit: 54406, thisYearProfit: 43342 },
      { product: "Black Watch", lastYearSale: 83, thisYearSale: 9, lastYearProfit: 423132, thisYearProfit: 312122 },
      { product: "Blue Band", lastYearSale: 38, thisYearSale: 5, lastYearProfit: 12321, thisYearProfit: 8500 },
      { product: "Blue T-Shirt", lastYearSale: 49, thisYearSale: 22, lastYearProfit: 745232, thisYearProfit: 65323 },
      { product: "Brown Purse", lastYearSale: 17, thisYearSale: 79, lastYearProfit: 643242, thisYearProfit: 500332 },
      {
        product: "Chakra Bracelet",
        lastYearSale: 52,
        thisYearSale: 65,
        lastYearProfit: 421132,
        thisYearProfit: 150005,
      },
      {
        product: "Galaxy Earrings",
        lastYearSale: 82,
        thisYearSale: 12,
        lastYearProfit: 131211,
        thisYearProfit: 100214,
      },
      { product: "Game Controller", lastYearSale: 44, thisYearSale: 45, lastYearProfit: 66442, thisYearProfit: 53322 },
      { product: "Gaming Set", lastYearSale: 90, thisYearSale: 56, lastYearProfit: 765442, thisYearProfit: 296232 },
      { product: "Gold Phone Case", lastYearSale: 75, thisYearSale: 54, lastYearProfit: 21212, thisYearProfit: 12533 },
    ];
  },

  methods: {
    formatCurrency(value) {
      return value.toLocaleString("en-US", { style: "currency", currency: "USD" });
    },
  },
  computed: {
    lastYearTotal() {
      let total = 0;
      for (const sale of this.sales) {
        total += sale.lastYearProfit;
      }
      return this.formatCurrency(total);
    },
    thisYearTotal() {
      let total = 0;
      for (const sale of this.sales) {
        total += sale.thisYearProfit;
      }
      return this.formatCurrency(total);
    },
  },
});
</script>

<style lang="scss" scoped>
.p-rowgroup-footer td {
  font-weight: 700;
}

::v-deep(.p-rowgroup-header) {
  span {
    font-weight: 700;
  }

  .p-row-toggler {
    vertical-align: middle;
    margin-right: 0.25rem;
  }
}
</style>

// [ // { // "id":1000, // "name":"James Butt", // "country":{ // "name":"Algeria", // "code":"dz" // }, //
"company":"Benton, John B Jr", // "date":"2015-09-13", // "status":"unqualified", // "verified":true, // "activity":17,
// "representative":{ // "name":"Ioni Bowcher", // "image":"ionibowcher.png" // }, // "balance":70663 // }, // { //
"id":1001, // "name":"Josephine Darakjy", // "country":{ // "name":"Egypt", // "code":"eg" // }, // "company":"Chanay,
Jeffrey A Esq", // "date":"2019-02-09", // "status":"proposal", // "verified":true, // "activity":0, //
"representative":{ // "name":"Amy Elsner", // "image":"amyelsner.png" // }, // "balance":82429 // }, // { // "id":1002,
// "name":"Art Venere", // "country":{ // "name":"Panama", // "code":"pa" // }, // "company":"Chemel, James L Cpa", //
"date":"2017-05-13", // "status":"qualified", // "verified":false, // "activity":63, // "representative":{ //
"name":"Asiya Javayant", // "image":"asiyajavayant.png" // }, // "balance":28334 // }, // { // "id":1003, //
"name":"Lenna Paprocki", // "country":{ // "name":"Slovenia", // "code":"si" // }, // "company":"Feltz Printing
Service", // "date":"2020-09-15", // "status":"new", // "verified":false, // "activity":37, // "representative":{ //
"name":"Xuxue Feng", // "image":"xuxuefeng.png" // }, // "balance":88521 // }, // { // "id":1004, // "name":"Donette
Foller", // "country":{ // "name":"South Africa", // "code":"za" // }, // "company":"Printing Dimensions", //
"date":"2016-05-20", // "status":"proposal", // "verified":true, // "activity":33, // "representative":{ //
"name":"Asiya Javayant", // "image":"asiyajavayant.png" // }, // "balance":93905 // }, // { // "id":1005, //
"name":"Simona Morasca", // "country":{ // "name":"Egypt", // "code":"eg" // }, // "company":"Chapman, Ross E Esq", //
"date":"2018-02-16", // "status":"qualified", // "verified":false, // "activity":68, // "representative":{ //
"name":"Ivan Magalhaes", // "image":"ivanmagalhaes.png" // }, // "balance":50041 // }, // { // "id":1006, //
"name":"Mitsue Tollner", // "country":{ // "name":"Paraguay", // "code":"py" // }, // "company":"Morlong Associates", //
"date":"2018-02-19", // "status":"renewal", // "verified":true, // "activity":54, // "representative":{ // "name":"Ivan
Magalhaes", // "image":"ivanmagalhaes.png" // }, // "balance":58706 // }, // { // "id":1007, // "name":"Leota Dilliard",
// "country":{ // "name":"Serbia", // "code":"rs" // }, // "company":"Commercial Press", // "date":"2019-08-13", //
"status":"renewal", // "verified":true, // "activity":69, // "representative":{ // "name":"Onyama Limba", //
"image":"onyamalimba.png" // }, // "balance":26640 // }, // { // "id":1008, // "name":"Sage Wieser", // "country":{ //
"name":"Egypt", // "code":"eg" // }, // "company":"Truhlar And Truhlar Attys", // "date":"2018-11-21", //
"status":"unqualified", // "verified":true, // "activity":76, // "representative":{ // "name":"Ivan Magalhaes", //
"image":"ivanmagalhaes.png" // }, // "balance":65369 // }, // { // "id":1009, // "name":"Kris Marrier", // "country":{
// "name":"Mexico", // "code":"mx" // }, // "company":"King, Christopher A Esq", // "date":"2015-07-07", //
"status":"proposal", // "verified":false, // "activity":3, // "representative":{ // "name":"Onyama Limba", //
"image":"onyamalimba.png" // }, // "balance":63451 // }, // { // "id":1010, // "name":"Minna Amigon", // "country":{ //
"name":"Romania", // "code":"ro" // }, // "company":"Dorl, James J Esq", // "date":"2018-11-07", //
"status":"qualified", // "verified":false, // "activity":38, // "representative":{ // "name":"Anna Fali", //
"image":"annafali.png" // }, // "balance":71169 // }, // { // "id":1011, // "name":"Abel Maclead", // "country":{ //
"name":"Singapore", // "code":"sg" // }, // "company":"Rangoni Of Florence", // "date":"2017-03-11", //
"status":"qualified", // "verified":true, // "activity":87, // "representative":{ // "name":"Bernardo Dominic", //
"image":"bernardodominic.png" // }, // "balance":96842 // }, // { // "id":1012, // "name":"Kiley Caldarera", //
"country":{ // "name":"Serbia", // "code":"rs" // }, // "company":"Feiner Bros", // "date":"2015-10-20", //
"status":"unqualified", // "verified":false, // "activity":80, // "representative":{ // "name":"Onyama Limba", //
"image":"onyamalimba.png" // }, // "balance":92734 // }, // { // "id":1013, // "name":"Graciela Ruta", // "country":{ //
"name":"Chile", // "code":"cl" // }, // "company":"Buckley Miller & Wright", // "date":"2016-07-25", //
"status":"negotiation", // "verified":false, // "activity":59, // "representative":{ // "name":"Amy Elsner", //
"image":"amyelsner.png" // }, // "balance":45250 // }, // { // "id":1014, // "name":"Cammy Albares", // "country":{ //
"name":"Philippines", // "code":"ph" // }, // "company":"Rousseaux, Michael Esq", // "date":"2019-06-25", //
"status":"new", // "verified":true, // "activity":90, // "representative":{ // "name":"Asiya Javayant", //
"image":"asiyajavayant.png" // }, // "balance":30236 // }, // { // "id":1015, // "name":"Mattie Poquette", //
"country":{ // "name":"Venezuela", // "code":"ve" // }, // "company":"Century Communications", // "date":"2017-12-12",
// "status":"negotiation", // "verified":false, // "activity":52, // "representative":{ // "name":"Anna Fali", //
"image":"annafali.png" // }, // "balance":64533 // } // ]
