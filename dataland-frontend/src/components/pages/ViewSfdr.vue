<template>
  <ViewFrameworkBase
    :companyID="companyID"
    dataType="sfdr"
    @updateActiveDataMetaInfoForChosenFramework="handleUpdateActiveDataMetaInfo"
  >
    <template v-slot:content v-if="receivedSfdrDataIds.length > 0">
      <div class="grid">
        <div class="col-12">
          <SfdrPanel :companyId="companyID" />
        </div>
      </div>
    </template>
    <div v-if="loading" class="col-12 text-left">
      <h2>Checking if SFDR data is available...</h2>
    </div>
    <div v-if="!loading && receivedSfdrDataIds.length === 0" class="col-12 text-left">
      <h2>No SFDR data</h2>
    </div>
  </ViewFrameworkBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import SfdrPanel from "@/components/resources/frameworkDataSearch/sfdr/SfdrPanel.vue";
import { defineComponent } from "vue";
import { DataMetaInformation } from "@clients/backend";

export default defineComponent({
  name: "ViewSfdr",
  components: { ViewFrameworkBase, SfdrPanel },
  props: {
    companyID: {
      type: String,
    },
  },
  data() {
    return {
      loading: true,
      receivedSfdrDataIds: [] as string[],
    };
  },
  methods: {
    /**
     * TODO Stores the received data IDs from the "updateDataId" event and terminates the loading-state of the component.
     *
     * @param receivedMapOfReportingPeriodsToActiveDataMetaInfo 1-to-1 map between reporting periods and corresponding
     * active data meta information objects
     */
    handleUpdateActiveDataMetaInfo(receivedMapOfReportingPeriodsToActiveDataMetaInfo: DataMetaInformation[]) {
      this.receivedSfdrDataIds = Array.from(receivedMapOfReportingPeriodsToActiveDataMetaInfo.values()).map(
        (dataMetaInfo) => dataMetaInfo.dataId
      );
      // TODO can we remove the sorting logic for same-year datasets from the lksg-view-page now?  because we receive only one per year now (because of "latest" setting)
      this.loading = false;
    },
  },
});
</script>
