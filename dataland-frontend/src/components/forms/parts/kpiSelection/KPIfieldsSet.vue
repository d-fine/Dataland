<template>
  <div class="form-field vertical-middle">
    <InputSwitch
      inputId="dataPointIsAvailableSwitch"
      @click="dataPointAvailableToggle"
      v-model="dataPointIsAvailable"
    />
    <h5 class="ml-2">Data point is available</h5>
  </div>

  <div v-if="dataPointIsAvailable">
    <div class="form-field">
      <UploadFormHeader name="Eligible Revenue (%) *" explanation="Eligible Revenue (%) *" />
      <FormKit
        type="number"
        name="value"
        validation-label=""
        placeholder="Value %"
        step="0.01"
        min="0"
        validation="number|between:0,100"
        :inner-class="{
          short: true,
        }"
      />
    </div>

    <!-- Data source -->
    <div class="form-field">
      <FormKit type="group" name="dataSource">
        <h4 class="mt-0">Data source</h4>
        <div class="next-to-each-other">
          <div class="flex-1">
            <UploadFormHeader :name="KpiNameMappings.report ?? ''" :explanation="KpiInfoMappings.report ?? ''" />
            <FormKit
              type="select"
              name="report"
              placeholder="Select a report"
              validation-label="Select a report"
              :options="['None...', ...this.files.filesNames]"
            />
          </div>
          <div>
            <UploadFormHeader :name="KpiNameMappings.page ?? ''" :explanation="KpiInfoMappings.page ?? ''" />
            <FormKit outer-class="w-100" type="number" name="page" placeholder="Page" validation-label="Page" />
          </div>
        </div>
        <div>
          <UploadFormHeader :name="KpiNameMappings.tagName ?? ''" :explanation="KpiInfoMappings.tagName ?? ''" />
          <FormKit outer-class="short" type="text" name="tagName" placeholder="Tag Name" validation-label="Tag Name" />
        </div>
      </FormKit>
    </div>
    <!-- Data quality -->
    <div class="form-field">
      <UploadFormHeader name="Data quality" explanation="Data quality" />
      <div class="lg:col-6 md:col-6 col-12 p-0">
        <FormKit
          type="select"
          name="quality"
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
      name="comment"
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
import { useFilesUploadedStore } from "@/stores/filesUploaded";

export default defineComponent({
  name: "KPIfieldsSet",
  components: { UploadFormHeader, FormKit, InputSwitch },
  emits: ["dataPointAvailableToggle"],
  data: () => ({
    files: useFilesUploadedStore(),
    dataPointIsAvailable: true,
    dataQualityList: ["Audited", "Reported", "Estimated", "Incomplete", "N/A"],
  }),
  props: {
    name: {
      type: String,
    },
    KpiInfoMappings: {
      type: Object,
      default: null,
    },
    KpiNameMappings: {
      type: Object,
      default: null,
    },
  },
  methods: {
    dataPointAvailableToggle() {
      this.dataPointIsAvailable = !this.dataPointIsAvailable;
      this.$emit("dataPointAvailableToggle", this.dataPointIsAvailable);
    },
  },
});
</script>
