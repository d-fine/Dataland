<template>
  <div class="grid surface-800 align-items-center">
    <div class="col-1 text-left ml-8">
      <h2>Results</h2>
    </div>
    <div class="col-3 col-offset-7 surface-0 p-card " style="border-radius: 0.2rem">
      <div class="grid align-items-center">
        <div class="col-6 text-left">
          <span class="font-semibold">Avg Green asset ratio </span>
        </div>
        <div class="col-6 text-right text-green-500">
          <span class="font-semibold text-xl">78</span> <span> % </span>
        </div>
      </div>
    </div>
    <div class="col-12 text-left pl-8 pr-8" >
      <template v-if="action">
        <DataTable v-if="data" :value="data" responsive-layout="scroll">
          <Column field="companyName" header="COMPANY" :sortable="true" class="surface-0" >
          </Column>
          <Column field="companyId" header="Company ID" :sortable="true" class="surface-0"> </Column>
          <Column field="companyId" header="" class="surface-0"> <template #body="{data}">
            <router-link :to="/companies/ + data.companyId" class="text-primary no-underline font-bold"> <span> VIEW</span> <span class="ml-3">></span></router-link>
          </template> </Column>
        </DataTable>
        <p v-else>The resource you requested does not exist yet. You can create it:
          <router-link to="/upload">Create Data</router-link>
        </p>
      </template>
    </div>
  </div>
</template>

<script>
import DataTable from "primevue/datatable";
import Column from "primevue/column";

export default {
  name: "EuTaxoSearchResults",
  components: {DataTable, Column},
  props: {
    data: {
      type: Object,
      default: null
    },
    action: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>

</style>