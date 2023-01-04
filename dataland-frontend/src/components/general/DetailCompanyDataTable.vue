
<template>
  <div>
    <Toast />

<!--    <div class="card">-->
<!--      <h5>Subheader Grouping</h5>-->
<!--      <DataTable-->
<!--          :value="customers"-->
<!--          rowGroupMode="subheader"-->
<!--          groupRowsBy="representative.name"-->
<!--          sortMode="single"-->
<!--          sortField="representative.name"-->
<!--          :sortOrder="1"-->
<!--          scrollable-->
<!--          scrollHeight="100vh"-->
<!--      >-->
<!--        <Column field="representative.name" header="Representative" style="min-width:200px"></Column>-->
<!--        <Column field="name" header="KPIs" style="min-width:200px"></Column>-->
<!--        <Column field="country" header="2023" style="min-width:200px">-->
<!--          <template #body="slotProps">-->

<!--            <span class="image-text">{{slotProps.data.country.name}}</span>-->
<!--          </template>-->
<!--        </Column>-->
<!--        <Column field="company" header="2022" style="min-width:200px"></Column>-->
<!--        <Column field="status" header="2021" style="min-width:200px">-->
<!--          <template #body="slotProps">-->
<!--            <span :class="'customer-badge status-' + slotProps.data.status">{{slotProps.data.status}}</span>-->
<!--          </template>-->
<!--        </Column>-->
<!--        <Column field="date" header="2020" style="min-width:200px"></Column>-->

<!--        <template #groupheader="slotProps">-->
<!--          <span class="image-text">{{slotProps.data.country.name}}</span>-->
<!--        </template>-->
<!--        <template #groupfooter="slotProps">-->
<!--          <td style="min-width: 80%">-->
<!--            <div style="text-align: right; width: 100%">Total Customers</div>-->
<!--          </td>-->
<!--          <td style="width: 20%">{{calculateCustomerTotal(slotProps.data.representative.name)}}</td>-->
<!--        </template>-->
<!--      </DataTable>-->
<!--    </div>-->

    <div class="card">
      <h5>Expandable Row Groups</h5>
      <DataTable
          :value="customers"
          rowGroupMode="subheader"
          groupRowsBy="group"
          sortMode="single"
          sortField="group"
          :sortOrder="1"
          responsiveLayout="scroll"
          :expandableRowGroups="true"
          v-model:expandedRowGroups="expandedRowGroups"
          @rowgroupExpand="onRowGroupExpand"
          @rowgroupCollapse="onRowGroupCollapse"
      >
        <Column field="dataDate" header="Data Date"></Column>
        <Column field="companyLegalForm" header="companyLegalForm"></Column>
        <Column field="totalRevenue" header="totalRevenue">
          <template #body="slotProps">
            <span class="image-text">{{slotProps.data.totalRevenue}}</span>
          </template>
        </Column>
        <Column field="shareOfTemporaryWorkers" header="shareOfTemporaryWorkers">
          <template #body="slotProps">
            <span>{{slotProps.data.shareOfTemporaryWorkers}}</span>
          </template>
        </Column>
        <template #groupheader="slotProps">
          <span class="image-text">{{slotProps.data.group}}</span>
        </template>
        <template #groupfooter="slotProps">
          <td colspan="4" style="text-align: right">Total Customers</td>
        </template>
      </DataTable>
    </div>

<!--    <div class="card">-->
<!--      <h5>RowSpan Grouping</h5>-->
<!--      <DataTable :value="customers" rowGroupMode="rowspan" groupRowsBy="representative.name"-->
<!--                 sortMode="single" sortField="representative.name" :sortOrder="1" responsiveLayout="scroll">-->
<!--        <Column header="#" headerStyle="width:3em">-->
<!--          <template #body="slotProps">-->
<!--            {{slotProps.index}}-->
<!--          </template>-->
<!--        </Column>-->
<!--        <Column field="representative.name" header="Representative">-->
<!--          <template #body="slotProps">-->
<!--            <img :alt="slotProps.data.representative.name" src="https://www.primefaces.org/wp-content/uploads/2020/05/placeholder.png" width="32" style="vertical-align: middle" />-->
<!--            <span class="image-text">{{slotProps.data.representative.name}}</span>-->
<!--          </template>-->
<!--        </Column>-->
<!--        <Column field="name" header="Name"></Column>-->
<!--        <Column field="country" header="Country">-->
<!--          <template #body="slotProps">-->
<!--            <img src="https://www.primefaces.org/wp-content/uploads/2020/05/placeholder.png" width="30" />-->
<!--            <span class="image-text">{{slotProps.data.country.name}}</span>-->
<!--          </template>-->
<!--        </Column>-->
<!--        <Column field="company" header="Company"></Column>-->
<!--        <Column field="status" header="Status">-->
<!--          <template #body="slotProps">-->
<!--            <span :class="'customer-badge status-' + slotProps.data.status">{{slotProps.data.status}}</span>-->
<!--          </template>-->
<!--        </Column>-->
<!--        <Column field="date" header="Date"></Column>-->
<!--      </DataTable>-->
<!--    </div>-->

  </div>
</template>

<script>
import {defineComponent} from "vue";
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

export default defineComponent( {
  name: "DetailCompanyDataTable",
  components: { DataTable, Column },
  data() {
    return {
      customers: null,
      expandedRowGroups: null
    }
  },
  props: {
    dataSet: {
      type: [],
      default: [],
    },
  },
  mounted() {
    this.customers = this.dataSet;
  },

  methods: {
    onRowGroupExpand(event) {
      this.$toast.add({severity: 'info', summary: 'Row Group Expanded', detail: 'Value: ' + event.data, life: 3000});
    },
    onRowGroupCollapse(event) {
      this.$toast.add({severity: 'success', summary: 'Row Group Collapsed', detail: 'Value: ' + event.data, life: 3000});
    },
    calculateCustomerTotal(name) {
      let total = 0;

      if (this.customers) {
        for (let customer of this.customers) {
          if (customer.country.name === name) {
            total++;
          }
        }
      }

      return total;
    }
  }
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
    margin-right: .25rem;
  }
}
</style>







// [
//   {
//     "id":1000,
//     "name":"James Butt",
//     "country":{
//       "name":"Algeria",
//       "code":"dz"
//     },
//     "company":"Benton, John B Jr",
//     "date":"2015-09-13",
//     "status":"unqualified",
//     "verified":true,
//     "activity":17,
//     "representative":{
//       "name":"Ioni Bowcher",
//       "image":"ionibowcher.png"
//     },
//     "balance":70663
//   },
//   {
//     "id":1001,
//     "name":"Josephine Darakjy",
//     "country":{
//       "name":"Egypt",
//       "code":"eg"
//     },
//     "company":"Chanay, Jeffrey A Esq",
//     "date":"2019-02-09",
//     "status":"proposal",
//     "verified":true,
//     "activity":0,
//     "representative":{
//       "name":"Amy Elsner",
//       "image":"amyelsner.png"
//     },
//     "balance":82429
//   },
//   {
//     "id":1002,
//     "name":"Art Venere",
//     "country":{
//       "name":"Panama",
//       "code":"pa"
//     },
//     "company":"Chemel, James L Cpa",
//     "date":"2017-05-13",
//     "status":"qualified",
//     "verified":false,
//     "activity":63,
//     "representative":{
//       "name":"Asiya Javayant",
//       "image":"asiyajavayant.png"
//     },
//     "balance":28334
//   },
//   {
//     "id":1003,
//     "name":"Lenna Paprocki",
//     "country":{
//       "name":"Slovenia",
//       "code":"si"
//     },
//     "company":"Feltz Printing Service",
//     "date":"2020-09-15",
//     "status":"new",
//     "verified":false,
//     "activity":37,
//     "representative":{
//       "name":"Xuxue Feng",
//       "image":"xuxuefeng.png"
//     },
//     "balance":88521
//   },
//   {
//     "id":1004,
//     "name":"Donette Foller",
//     "country":{
//       "name":"South Africa",
//       "code":"za"
//     },
//     "company":"Printing Dimensions",
//     "date":"2016-05-20",
//     "status":"proposal",
//     "verified":true,
//     "activity":33,
//     "representative":{
//       "name":"Asiya Javayant",
//       "image":"asiyajavayant.png"
//     },
//     "balance":93905
//   },
//   {
//     "id":1005,
//     "name":"Simona Morasca",
//     "country":{
//       "name":"Egypt",
//       "code":"eg"
//     },
//     "company":"Chapman, Ross E Esq",
//     "date":"2018-02-16",
//     "status":"qualified",
//     "verified":false,
//     "activity":68,
//     "representative":{
//       "name":"Ivan Magalhaes",
//       "image":"ivanmagalhaes.png"
//     },
//     "balance":50041
//   },
//   {
//     "id":1006,
//     "name":"Mitsue Tollner",
//     "country":{
//       "name":"Paraguay",
//       "code":"py"
//     },
//     "company":"Morlong Associates",
//     "date":"2018-02-19",
//     "status":"renewal",
//     "verified":true,
//     "activity":54,
//     "representative":{
//       "name":"Ivan Magalhaes",
//       "image":"ivanmagalhaes.png"
//     },
//     "balance":58706
//   },
//   {
//     "id":1007,
//     "name":"Leota Dilliard",
//     "country":{
//       "name":"Serbia",
//       "code":"rs"
//     },
//     "company":"Commercial Press",
//     "date":"2019-08-13",
//     "status":"renewal",
//     "verified":true,
//     "activity":69,
//     "representative":{
//       "name":"Onyama Limba",
//       "image":"onyamalimba.png"
//     },
//     "balance":26640
//   },
//   {
//     "id":1008,
//     "name":"Sage Wieser",
//     "country":{
//       "name":"Egypt",
//       "code":"eg"
//     },
//     "company":"Truhlar And Truhlar Attys",
//     "date":"2018-11-21",
//     "status":"unqualified",
//     "verified":true,
//     "activity":76,
//     "representative":{
//       "name":"Ivan Magalhaes",
//       "image":"ivanmagalhaes.png"
//     },
//     "balance":65369
//   },
//   {
//     "id":1009,
//     "name":"Kris Marrier",
//     "country":{
//       "name":"Mexico",
//       "code":"mx"
//     },
//     "company":"King, Christopher A Esq",
//     "date":"2015-07-07",
//     "status":"proposal",
//     "verified":false,
//     "activity":3,
//     "representative":{
//       "name":"Onyama Limba",
//       "image":"onyamalimba.png"
//     },
//     "balance":63451
//   },
//   {
//     "id":1010,
//     "name":"Minna Amigon",
//     "country":{
//       "name":"Romania",
//       "code":"ro"
//     },
//     "company":"Dorl, James J Esq",
//     "date":"2018-11-07",
//     "status":"qualified",
//     "verified":false,
//     "activity":38,
//     "representative":{
//       "name":"Anna Fali",
//       "image":"annafali.png"
//     },
//     "balance":71169
//   },
//   {
//     "id":1011,
//     "name":"Abel Maclead",
//     "country":{
//       "name":"Singapore",
//       "code":"sg"
//     },
//     "company":"Rangoni Of Florence",
//     "date":"2017-03-11",
//     "status":"qualified",
//     "verified":true,
//     "activity":87,
//     "representative":{
//       "name":"Bernardo Dominic",
//       "image":"bernardodominic.png"
//     },
//     "balance":96842
//   },
//   {
//     "id":1012,
//     "name":"Kiley Caldarera",
//     "country":{
//       "name":"Serbia",
//       "code":"rs"
//     },
//     "company":"Feiner Bros",
//     "date":"2015-10-20",
//     "status":"unqualified",
//     "verified":false,
//     "activity":80,
//     "representative":{
//       "name":"Onyama Limba",
//       "image":"onyamalimba.png"
//     },
//     "balance":92734
//   },
//   {
//     "id":1013,
//     "name":"Graciela Ruta",
//     "country":{
//       "name":"Chile",
//       "code":"cl"
//     },
//     "company":"Buckley Miller & Wright",
//     "date":"2016-07-25",
//     "status":"negotiation",
//     "verified":false,
//     "activity":59,
//     "representative":{
//       "name":"Amy Elsner",
//       "image":"amyelsner.png"
//     },
//     "balance":45250
//   },
//   {
//     "id":1014,
//     "name":"Cammy Albares",
//     "country":{
//       "name":"Philippines",
//       "code":"ph"
//     },
//     "company":"Rousseaux, Michael Esq",
//     "date":"2019-06-25",
//     "status":"new",
//     "verified":true,
//     "activity":90,
//     "representative":{
//       "name":"Asiya Javayant",
//       "image":"asiyajavayant.png"
//     },
//     "balance":30236
//   },
//   {
//     "id":1015,
//     "name":"Mattie Poquette",
//     "country":{
//       "name":"Venezuela",
//       "code":"ve"
//     },
//     "company":"Century Communications",
//     "date":"2017-12-12",
//     "status":"negotiation",
//     "verified":false,
//     "activity":52,
//     "representative":{
//       "name":"Anna Fali",
//       "image":"annafali.png"
//     },
//     "balance":64533
//   }
// ]