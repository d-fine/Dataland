<template>
  <div class="form-field">
    <div data-test="dataPointToggle" class="form-field border-none vertical-middle">
      <InputSwitch
        data-test="dataPointToggleButton"
        inputId="dataPointIsAvailableSwitch"
        @click="this.isItActive = false"
        v-model="isItActive"
      />
      <p>isItActive</p>
      {{ !!this.existingFieldValuesCategory }}
      {{ isItActive }}
      <h5 data-test="dataPointToggleTitle" class="m-2">
        {{ label }}
      </h5>
    </div>
    <KeepAlive>
      <ProcurementCategoryFormElementFields :isItActive="isItActive" :name="name" v-if="isItActive" />
    </KeepAlive>
  </div>
</template>

<script lang="ts">
import ProcurementCategoryFormElementFields from "@/components/forms/parts/elements/derived/ProcurementCategoryFormElementFields.vue";
import { defineComponent, KeepAlive } from "vue";
import InputSwitch from "primevue/inputswitch";
import { FormFieldProps } from "@/components/forms/parts/fields/BaseFormFieldProps";

export default defineComponent({
  name: "ProcurementCategoryFormElement",
  components: {
    InputSwitch,
    KeepAlive,
    ProcurementCategoryFormElementFields,
  },
  props: {
    ...FormFieldProps,
    existingFieldValuesCategory: {
      type: Object,
    },
  },
  data() {
    return {
      isItActive: !!this.existingFieldValuesCategory,
    };
  },
  methods: {},
});
</script>
