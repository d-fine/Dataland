<template>
  <ViewFrameworkBase
    :companyID="companyID"
    :dataType="dataType"
    @updateActiveDataMetaInfoForChosenFramework="handleUpdateActiveDataMetaInfo"
  >
    <template v-slot:content>
      <div class="grid">
        <div class="col-12">
          <LksgPanel v-if="dataType === DataTypeEnum.Lksg" :companyId="companyID" />
          <SfdrPanel v-if="dataType === DataTypeEnum.Sfdr" :companyId="companyID" />
        </div>
      </div>
      <div v-if="loading" class="col-12 text-left">
        <h2>{{ "Checking if " + humanizeString(dataType) + " data is available..." }}</h2>
      </div>
      <div v-if="!loading && receivedDataIds.length === 0" class="col-12 text-left">
        <h2>{{ "No " + humanizeString(dataType) + " data available" }}</h2>
      </div>
    </template>
  </ViewFrameworkBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import { defineComponent } from "vue";
import { DataMetaInformation } from "@clients/backend";
import { humanizeString } from "@/utils/StringHumanizer";
import LksgPanel from "@/components/resources/frameworkDataSearch/lksg/LksgPanel.vue";
import { DataTypeEnum } from "@clients/backend";
import SfdrPanel from "@/components/resources/frameworkDataSearch/sfdr/SfdrPanel.vue";

export default defineComponent({
  name: "ViewMultipleDatasetsDisplayBase",
  components: { SfdrPanel, LksgPanel, ViewFrameworkBase },
  props: {
    companyID: {
      type: String,
    },
    dataType: {
      type: String,
    },
  },
  data() {
    return {
      loading: true,
      receivedDataIds: [] as string[],
      humanizeString: humanizeString,
      DataTypeEnum,
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
      this.receivedDataIds = Array.from(receivedMapOfReportingPeriodsToActiveDataMetaInfo.values()).map(
        (dataMetaInfo) => dataMetaInfo.dataId
      );
      // TODO can we remove the sorting logic for same-year datasets from the lksg-view-page now?  because we receive only one per year now (because of "latest" setting)
      this.loading = false;
    },
  },
});
</script>
