<template>
  <!-- Level of assurance -->

  <FormKit name="assurance" type="group">
    <!-- Level of assurance -->
    <div class="form-field">
      <UploadFormHeader
        :label="euTaxonomyKpiNameMappings.assurance ?? ''"
        :description="euTaxonomyKpiInfoMappings.assurance ?? ''"
        :is-required="true"
      />
      <div class="lg:col-4 md:col-6 col-12 p-0">
        <FormKit
          type="select"
          name="value"
          placeholder="Please choose..."
          :validation-label="euTaxonomyKpiNameMappings.assurance ?? ''"
          validation="required"
          :options="assuranceData"
        />
      </div>
    </div>
    <!-- Assurance provider -->
    <div class="form-field">
      <UploadFormHeader
        :label="euTaxonomyKpiNameMappings.provider ?? ''"
        :description="euTaxonomyKpiInfoMappings.provider ?? ''"
      />
      <FormKit
        type="text"
        name="provider"
        :placeholder="euTaxonomyKpiNameMappings.provider ?? ''"
        :validation-label="euTaxonomyKpiNameMappings.provider ?? ''"
      />
    </div>

    <!-- Data source -->
    <div class="form-field">
      <FormKit type="group" name="dataSource">
        <h4 class="mt-0">Data source</h4>
        <div class="next-to-each-other">
          <div class="flex-1">
            <UploadFormHeader
              :label="euTaxonomyKpiNameMappings.report ?? ''"
              :description="euTaxonomyKpiInfoMappings.report ?? ''"
              :is-required="true"
            />
            <FormKit
              type="select"
              name="fileName"
              v-model="currentReportValue"
              placeholder="Select a report"
              :options="['None...', ...reportsName]"
            />
            <FormKit type="hidden" name="fileReference" :modelValue="fileReferenceAccordingToName" />
          </div>
          <div>
            <UploadFormHeader
              :label="euTaxonomyKpiNameMappings.page ?? ''"
              :description="euTaxonomyKpiInfoMappings.page ?? ''"
            />
            <FormKit
              outer-class="w-100"
              type="number"
              name="page"
              placeholder="Page"
              validation-label="Page"
              validation="min:0"
              step="1"
              min="0"
            />
          </div>
        </div>
      </FormKit>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { AssuranceDataPointValueEnum } from "@clients/backend";
import { type ObjectType } from "@/utils/UpdateObjectUtils";

export default defineComponent({
  name: "AssuranceFormField",
  inject: {
    injectReportsNameAndReferences: {
      from: "namesAndReferencesOfAllCompanyReportsForTheDataset",
      default: {} as ObjectType,
    },
  },
  components: { FormKit, UploadFormHeader },
  data() {
    return {
      euTaxonomyKpiNameMappings,
      euTaxonomyKpiInfoMappings,
      assuranceData: {
        None: humanizeStringOrNumber(AssuranceDataPointValueEnum.None),
        LimitedAssurance: humanizeStringOrNumber(AssuranceDataPointValueEnum.LimitedAssurance),
        ReasonableAssurance: humanizeStringOrNumber(AssuranceDataPointValueEnum.ReasonableAssurance),
      },
      currentReportValue: "",
    };
  },
  computed: {
    reportsName(): string[] {
      if (this.injectReportsNameAndReferences) {
        return Object.keys(this.injectReportsNameAndReferences);
      } else {
        return [];
      }
    },
    fileReferenceAccordingToName(): string {
      if (this.injectReportsNameAndReferences) {
        return this.injectReportsNameAndReferences[
          this.currentReportValue as keyof typeof this.injectReportsNameAndReferences
        ];
      } else {
        return "";
      }
    },
  },
  props: BaseFormFieldProps,
});
</script>
