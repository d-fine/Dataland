<template>
  <div class="mb-3">
    <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
    <div class="next-to-each-other">
      <FormKit
          type="text"
          name="value"
          v-model="currentValue"
          :validation-label="validationLabel ?? label"
          :validation="`number|${validation}`"
          :placeholder="unit ? `Value in ${unit}` : 'Value'"
          outer-class="short"
      />
      <div>
        <span>{{ unit }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { FormFieldPropsWithPlaceholder } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "NumberFormField",
  components: { FormKit, UploadFormHeader },
  props: FormFieldPropsWithPlaceholder,
});
</script>
