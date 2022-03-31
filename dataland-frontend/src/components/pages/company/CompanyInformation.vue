<template>
  <div v-if="companyInfo" class="grid">
      <div class="col md:col-10 col-offset-1">
        <h2>Company Information about {{companyInfo.data.companyName}} (ID: {{companyInfo.data.companyId}})</h2>

        <DataTable  v-if="response" :value="response.data" stripedRows responsive-layout="scroll" class="col col-6 col-offset-3">
          <Column field="Data ID" header="Data ID" :sortable="true" >
          </Column>
          <Column field="Data Type" header="Data Type" >
            <template #body="{data}">
              <router-link :to="/companies/ + data['Data ID']" class="text-primary font-bold">{{ data["Data Type"] }} </router-link>
            </template>
          </Column>

        </DataTable>
      </div>
  </div>
</template>

<script>
import {CompanyDataControllerApi, MetaDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
const companyApi = new CompanyDataControllerApi()
const metaDataApi = new MetaDataControllerApi()
const dataStore = new DataStore(metaDataApi.getListOfDataMetaInfo)
const companyStore = new DataStore(companyApi.getCompanyById)
export default {
  name: "CompanyInformation",
  components: { DataTable, Column},
  data() {
    return {
      response: null,
      companyInfo: null
    }
  },
  props: {
    companyID: {
      default: 1,
      type: Number
    }
  },
  created() {
    this.getCompanyDataset()
    this.getCompanyInformation()
  },
  methods: {
    async getCompanyInformation() {
      this.companyInfo = await companyStore.perform(this.companyID)
    },
    async getCompanyDataset() {
      this.response = await dataStore.perform(this.companyID, "")
    }
  }
}
</script>