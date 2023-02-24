<template>



  <div class="form-field vertical-middle">
    <InputSwitch inputId="dataPointIsAvailableSwitch" v-model="dataPointIsAvailable" />
    <h5 class="ml-2">Data point is available</h5>
  </div>

    <div v-if="dataPointIsAvailable">
    <div class="form-field">
      <UploadFormHeader
          name="Eligible Revenue (%) *"
          explanation="Eligible Revenue (%) *"
      />
      <FormKit
          type="number"
          name="shareOfTemporaryWorkers"
          :validation-label="lksgKpiNameMappings.shareOfTemporaryWorkers ? 'lll' : 'xxx'"
          placeholder="Value %"
          step="0.01"
          min="0"
          validation="required|number|between:0,100"
          :inner-class="{
                            short: true,
                          }"
      />
    </div>

  <!-- Data source -->
  <div class="form-field">
    <UploadFormHeader
        name="Data source *"
        explanation="Data source"
    />
    <div class="next-to-each-other">
      <!-- add options like a files from pinia -->
      <FormKit
          outer-class="flex-1"
          type="select"
          name="page"
          placeholder="Select a report"
          validation-label="Select a report"
          validation="required"
      />
      <FormKit
          outer-class="w-100"
          type="number"
          name="page"
          placeholder="Page"
          validation-label="Page"
          validation="required"
      />
    </div>
  </div>
    <!-- Data quality -->
    <div class="form-field">
      <UploadFormHeader
          name="Data quality"
          explanation="Data quality"
      />
    <div class="lg:col-6 md:col-6 col-12 p-0">
      <FormKit
          type="select"
          name="dataQuality"
          validation="required"
          validation-label="Data quality"
          placeholder="Data quality"
          :options="dataQualityList"
      />
    </div>
    </div>

    </div>

    <div class="form-field">
    <FormKit
        type="textarea"
        rows="10"
        placeholder="(Optional) Add comment that might help Quality Assurance to approve the datapoint. "
    />
    </div>

</template>

<script lang="ts">
import { defineComponent } from "vue";
import InputSwitch from "primevue/inputswitch";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import {
  lksgKpiNameMappings,
  lksgKpiInfoMappings,
} from "@/components/resources/frameworkDataSearch/DataModelsTranslations";

export default defineComponent({
  name: "KPIfieldsSet",
  components: { UploadFormHeader, FormKit, InputSwitch },
  data: () => ({
    dataPointIsAvailable: true,
    dataQualityList: ["Audited", "Reported", "Estimated", "Incomplete", "N/A"],
    lksgKpiNameMappings,
    lksgKpiInfoMappings,
  }),
  props: {
    name: {
      type: String,
    },
  },
});
</script>
