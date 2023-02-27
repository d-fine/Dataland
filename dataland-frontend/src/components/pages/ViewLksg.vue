<template>
  <ViewFrameworkBase
    :companyID="companyID"
    :dataType="DataTypeEnum.Lksg"
    @updateActiveDataMetaInfoForChosenFramework="handleUpdateActiveDataMetaInfo"
  >
    <template v-if="receivedLksgDataIds.length > 0">
      <div class="grid">
        <div class="col-12">
          <LksgPanel :companyId="companyID" />
        </div>
      </div>
    </template>
    <div v-if="loading" class="col-12 text-left">
      <h2>Checking if LkSG data is available...</h2>
    </div>
    <div v-if="!loading && receivedLksgDataIds.length === 0" class="col-12 text-left">
      <h2>No LkSG data</h2>
    </div>
  </ViewFrameworkBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import LksgPanel from "@/components/resources/frameworkDataSearch/lksg/LksgPanel.vue";
import { defineComponent } from "vue";
import { DataMetaInformation, DataTypeEnum } from "@clients/backend";

export default defineComponent({
  name: "ViewLksg",
  components: { ViewFrameworkBase, LksgPanel },
  props: {
    companyID: {
      type: String,
    },
  },
  data() {
    return {
      loading: true,
      receivedLksgDataIds: [] as string[],
      DataTypeEnum,
    };
  },
  methods: {
    /**
     * TODO Stores the received data IDs from the "updateDataId" event and terminates the loading-state of the component.
     *
     * @param receivedLksgDataIds Received LkSG data IDs
     * @param listOfReceivedDataMetaInfo
     */
    handleUpdateActiveDataMetaInfo(listOfReceivedDataMetaInfo: DataMetaInformation[]) {
      this.receivedLksgDataIds = listOfReceivedDataMetaInfo.map((dataMetaInfo) => dataMetaInfo.dataId);
      // TODO can we remove the sorting logic for same-year datasets from the lksg-view-page now?  because we receive only one per year now (because of "latest" setting)
      this.loading = false;
    },
  },
});
</script>
