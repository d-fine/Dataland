<template>
  <div v-if="toggleDataAvailable" data-test="dataPointToggle" class="form-field vertical-middle">
    <InputSwitch
      data-test="dataPointToggleButton"
      inputId="dataPointIsAvailableSwitch"
      @click="dataPointAvailableToggle"
      v-model="dataPointIsAvailable"
    />
    <h5 class="ml-2">
      {{ dataPointIsAvailable ? "Data point is available" : "Data point is not available" }}
    </h5>
  </div>

  <div v-if="dataPointIsAvailable">
    <div class="form-field">
      <UploadFormHeader name="Eligible Revenue (%) *" explanation="Eligible Revenue (%) *" />
      <FormKit
        type="number"
        name="value"
        validation-label=""
        placeholder="Value %"
        step="any"
        min="0"
        validation="number|between:0,100"
        :inner-class="{
          short: true,
        }"
      />
    </div>

    <div class="form-field">
      <FormKit type="group" name="dataSource">
        <h4 class="mt-0">Data source</h4>
        <div class="next-to-each-other">
          <div class="flex-1">
            <UploadFormHeader :name="kpiNameMappings.report ?? ''" :explanation="kpiInfoMappings.report ?? ''" />
            <FormKit
              type="select"
              name="report"
              placeholder="Select a report"
              validation="required"
              validation-label="Select a report"
              :options="['None...', ...this.files.filesNames]"
            />
          </div>
          <div>
            <UploadFormHeader :name="kpiNameMappings.page ?? ''" :explanation="kpiInfoMappings.page ?? ''" />
            <FormKit outer-class="w-100" type="number" name="page" placeholder="Page" validation-label="Page" />
          </div>
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
  name: "KPIfieldSet",
  components: { UploadFormHeader, FormKit, InputSwitch },
  emits: ["dataPointAvailableToggle"],
  data: () => ({
    files: useFilesUploadedStore(),
    dataPointIsAvailable: true,
    dataQualityList: ["NA", "Audited", "Reported", "Estimated", "Incomplete"],
  }),
  props: {
    name: {
      type: String,
    },
    kpiInfoMappings: {
      type: Object,
      default: null,
    },
    kpiNameMappings: {
      type: Object,
      default: null,
    },
    toggleDataAvailable: {
      type: Boolean,
      default: true,
    },
  },
  methods: {
    /**
     * Toggle dataPointIsAvailable variable value and emit event
     *
     */
    dataPointAvailableToggle(): void {
      this.dataPointIsAvailable = !this.dataPointIsAvailable;
      this.$emit("dataPointAvailableToggle", this.dataPointIsAvailable);
    },
  },
});
</script>
