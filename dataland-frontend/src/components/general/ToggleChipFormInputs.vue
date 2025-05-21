<template>
  <div data-test="toggleChipsFormInput">
    <template v-for="option in options" :key="option.name">
      <ToggleChip
        :label="option.name"
        :disabled="isDisabled(option.name)"
        @on-change="onToggleChange(option, $event)"
      />
    </template>
    <div class="hidden">
      <FormKit type="checkbox" v-model="value" :name="name" :options="checkboxOptions" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import ToggleChip from '@/components/general/ToggleChip.vue';

export type ToggleChipInputType = { name: string; value: boolean };

export default defineComponent({
  name: 'ToggleChipFormInputs',
  emits: ['changed'],
  components: {
    ToggleChip,
  },

  props: {
    name: {
      type: String,
      required: true,
    },
    options: {
      type: Array as () => Array<ToggleChipInputType>,
    },
    availableOptions: {
      type: Array as () => Array<ToggleChipInputType>,
      default: () => [],
    },
  },

  data() {
    return {
      value: [] as Array<string>,
    };
  },

  computed: {
    checkboxOptions() {
      return this.options?.map((option) => option.name) ?? [];
    },
  },

  methods: {
    /**
     * Checks if an option should be disabled based on availableOptions prop
     *
     * @param optionName The name of the option to check
     * @returns True if the option should be disabled, false otherwise
     */
    isDisabled(optionName: string): boolean {
      if (this.availableOptions === undefined) return false;
      return !this.availableOptions.some((option) => option.name === optionName);
    },

    /**
     * Handles the toggle change event from a ToggleChip
     *
     * @param option the object that changed
     * @param value current state of the toggle
     */
    onToggleChange(option: ToggleChipInputType, value: boolean) {
      if (!this.isDisabled(option.name)) {
        option.value = value;
        this.value = this.options?.filter((option) => option.value).map((option) => option.name) ?? [];
        this.$emit('changed');
      }
    },
  },
});
</script>
