<template>
  <FormKit type="group" :name="name">
    <div class="form-field">
      <div data-test="dataPointToggle" class="form-field border-none vertical-middle">
        <InputSwitch
          data-test="dataPointToggleButton"
          inputId="dataPointIsAvailableSwitch"
          @click="this.isItActive = false"
          v-model="isItActive"
        />
        <h5 data-test="dataPointToggleTitle" class="m-2">
          {{ label }}
        </h5>
      </div>
      <KeepAlive>
        <ProcurementCategoryFormElementFields v-if="isItActive" />
      </KeepAlive>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import ProcurementCategoryFormElementFields from "@/components/forms/parts/elements/derived/ProcurementCategoryFormElementFields.vue";
import { defineComponent } from "vue";
import { FormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import InputSwitch from "primevue/inputswitch";
import { KeepAlive } from "vue";

export default defineComponent({
  name: "ProcurementCategoryFormElement",
  components: {
    FormKit,
    InputSwitch,
    KeepAlive,
    ProcurementCategoryFormElementFields,
  },
  data() {
    return {
      isItActive: false,
    };
  },
  props: FormFieldProps,
  methods: {},
});
</script>
