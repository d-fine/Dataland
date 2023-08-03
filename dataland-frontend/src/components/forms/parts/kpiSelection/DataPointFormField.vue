<template>
  <template v-if="evidenceDesired">
    <FormKit type="group" :name="name">
      <div class="mb-3">
        <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
        <div class="next-to-each-other">
          <FormKit
            type="text"
            name="value"
            v-model="currentValue"
            :validation-label="validationLabel ?? label"
            :validation="`number|${validation}`"
            placeholder="Value"
          />
          <span v-if="unit" class="form-field-label pb-3">in {{ unit }}</span>
          <FormKit
            v-if="options"
            type="select"
            name="unit"
            v-model="currentUnit"
            placeholder="Unit"
            :options="options"
            inner-class="medium"
          />
        </div>
      </div>
      <!-- //TODO make the label and description modular instead of being hardcoded -->
      <div>
        <FormKit type="group" name="dataSource">
          <div class="next-to-each-other">
            <div class="flex-1">
              <UploadFormHeader :label="`${label} Report`" :description="'Upload Report'" />
              <FormKit
                type="select"
                name="report"
                v-model="currentReportValue"
                placeholder="Select a report"
                :options="['None...', ...displayreportsName]"
                :plugins="[selectNothingIfNotExistsFormKitPlugin]"
              />
            </div>
            <div>
              <UploadFormHeader :label="'Page'" :description="'Page where information was found'" />
              <FormKit
                outer-class="w-100"
                v-model="currentPageValue"
                type="number"
                name="page"
                placeholder="Page"
                validation-label="Page"
                step="1"
                min="0"
                validation="min:0"
              />
            </div>
          </div>
        </FormKit>
      </div>

      <!-- Data quality -->
      <div class="mb-4">
        <UploadFormHeader
          :label="`${label} Quality`"
          description="The level of confidence associated to the value."
          :is-required="true"
        />
        <div class="md:col-6 col-12 p-0">
          <FormKit
            data-test="dataQuality"
            type="select"
            v-model="currentQualityValue"
            name="quality"
            :validation="'required'"
            validation-label="Data quality"
            placeholder="Data quality"
            :options="qualityOptions"
          />
        </div>
      </div>
      <div class="form-field">
        <FormKit
          type="textarea"
          name="comment"
          placeholder="(Optional) Add comment that might help Quality Assurance to approve the datapoint. "
        />
      </div>
    </FormKit>
  </template>

  <template v-else>
    <FormKit type="group" :name="name">
      <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
      <FormKit
        type="text"
        :name="name"
        v-model="currentValue"
        :validation-label="validationLabel ?? label"
        :validation="`number|${validation}`"
        :placeholder="placeholder"
        :inner-class="innerClass"
      />
    </FormKit>
  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormKit } from "@formkit/vue";
import { QualityOptions } from "@clients/backend";
import { selectNothingIfNotExistsFormKitPlugin } from "@/utils/FormKitPlugins";
import { DataPointFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "DataPointFormField",
  components: { UploadFormHeader, FormKit },
  inject: {
    injectReportsName: {
      from: "namesOfAllCompanyReportsForTheDataset",
      default: [] as string[],
    },
  },
  computed: {
    displayreportsName(): string[] {
      return (this.reportsName ?? this.injectReportsName) as string;
    },
    // isUnit(): boolean {
    //   return
    // }
  },
  data() {
    return {
      qualityOptions: Object.values(QualityOptions).map((qualityOption: string) => ({
        label: qualityOption,
        value: qualityOption,
      })),
      currentValue: "",
      currentUnit: "",
      currentReportValue: "",
      currentPageValue: "",
      currentQualityValue: "",
    };
  },
  emits: ["documentUpdated"],
  props: {
    ...DataPointFormFieldProps,
  },
  methods: {
    selectNothingIfNotExistsFormKitPlugin,
  },
});
</script>
