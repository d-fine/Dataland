<template>
  <div data-test="AddressFormElement">
    <ActivitySelector />

    <div class="form-field">
      <FinancialShareFormField />
    </div>
    <div class="form-field">
      <YesNoFormField
        :name="'nfrdMandatory'"
        description="euTaxonomyKpiInfoMappings.nfrdMandatory"
        label="euTaxonomyKpiNameMappings.nfrdMandatory"
      />
    </div>
    <div class="form-field">
      <YesNoFormField
          :name="'nfrdMandatory'"
          description="euTaxonomyKpiInfoMappings.nfrdMandatory"
          label="euTaxonomyKpiNameMappings.nfrdMandatory"
      />
    </div>
    <div class="form-field">
      <YesNoFormField
          :name="'nfrdMandatory'"
          description="euTaxonomyKpiInfoMappings.nfrdMandatory"
          label="euTaxonomyKpiNameMappings.nfrdMandatory"
      />
    </div>
    <div class="form-field">
      <YesNoFormField
          :name="'nfrdMandatory'"
          description="euTaxonomyKpiInfoMappings.nfrdMandatory"
          label="euTaxonomyKpiNameMappings.nfrdMandatory"
      />
    </div>
    <div class="form-field">
      <YesNoFormField
          :name="'nfrdMandatory'"
          description="euTaxonomyKpiInfoMappings.nfrdMandatory"
          label="euTaxonomyKpiNameMappings.nfrdMandatory"
      />
    </div>
    <div class="form-field">
      <YesNoFormField
          :name="'nfrdMandatory'"
          description="euTaxonomyKpiInfoMappings.nfrdMandatory"
          label="euTaxonomyKpiNameMappings.nfrdMandatory"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import StringBadgeListFormField from "@/components/forms/parts/fields/StringBadgeListFormField.vue";
import Tree from "primevue/tree";
import Checkbox from "primevue/checkbox";
import ActivitySelector from "@/components/forms/parts/elements/derived/ActivitySelector.vue";
import MultiSelectFormField from "@/components/forms/parts/fields/MultiSelectFormField.vue";
import MultiSelectFormElement from "@/components/forms/parts/elements/basic/MultiSelectFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import FinancialShareFormField from "@/components/forms/parts/kpiSelection/FinancialShareFormField.vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";

export default defineComponent({
  name: "AlignedActivitiesFormElements",
  components: {
    YesNoFormField,
    UploadFormHeader,
    MultiSelectFormElement,
    InputTextFormField,
    FreeTextFormField,
    StringBadgeListFormField,
    Tree,
    Checkbox,
    ActivitySelector,
    MultiSelectFormField,
    FinancialShareFormField,
  },
  data: () => ({}),
});
</script>