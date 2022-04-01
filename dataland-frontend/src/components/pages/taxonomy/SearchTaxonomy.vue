<template>
  <div class="grid align-items-center">
    <div class="col-8 text-left">
      <h1 class="pl-3">Search EU Taxonomy data</h1>

    </div>
    <div class="col-4 text-right font-semibold">
      <span class="pr-3">Welcome to Dataland, Roger</span>
    </div>

  </div>
  <div class="grid">
    <div class="col-8 pl-3">
      <span class="p-fluid">
        <span class="p-input-icon-left ">
            <i class="pi pi-search"/>
            <InputText type="text" v-model="value" placeholder="Search by company name, CIN or ISIN"/>
        </span>
      </span>
    </div>
    <div class="col-2 text-left">

    <Button @click="getCompanyByName(true)" label="Search" />
    </div>
  </div>
  <div class="grid">
    <div class="col-12">
      <DataTable>
        <Column></Column>
      </DataTable>
    </div>
  </div>
</template>

<script>
import InputText from 'primevue/inputtext';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';

export default {
  name: "SearchTaxonomy",
  components: {InputText, DataTable, Column, Button},
  data() {
    return {
      value: null
    }
  }
}
</script>

<style scoped>

</style>