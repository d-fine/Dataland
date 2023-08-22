<template>
  <div v-if="!generalCategory" data-test="dataPointToggle" class="form-field vertical-middle">
    <InputSwitch
      data-test="dataPointToggleButton"
      inputId="dataPointIsAvailableSwitch"
      @click="dataPointAvailableToggle"
      v-model="dataPointIsAvailable"
    />
    <h5 data-test="dataPointToggleTitle" class="ml-2">
      {{ dataPointIsAvailable ? "Data point is available" : "Data point is not available" }}
    </h5>
  </div>
  <div v-if="dataPointIsAvailable">
    <slot></slot>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import InputSwitch from "primevue/inputswitch";

export default defineComponent({
  name: "SubcategoryToggleWrapper",
  components: { InputSwitch },
  emits: ["dataPointAvailableToggle"],
  data: () => ({
    dataPointIsAvailable: true,

  }),
  props: {
    subcategoryName: {
      type: String,
    },
  },
  computed: {
    generalCategory(): boolean {
      return this.subcategoryName === "general";
    },
  },
  methods: {
    /**
     * Toggle dataPointIsAvailable variable value and emit event
     *
     */
    dataPointAvailableToggle(): void {
      this.dataPointIsAvailable = !this.dataPointIsAvailable;
    },
  },
});
</script>
