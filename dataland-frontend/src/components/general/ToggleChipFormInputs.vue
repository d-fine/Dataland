<template>
  <div data-test="toggleChipsFormInput">
    <template v-for="option in options" :key="option.name">
      <ToggleChip :label="option.name" @on-change="onToggleChange(option, $event)" />
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
     * @param option the object that changed
     * @param value current state of the toggle
     */
    onToggleChange(option: ToggleChipInputType, value: boolean) {
      option.value = value;
      this.value = this.options?.filter((option) => option.value).map((option) => option.name) ?? [];
      this.$emit('changed');
    },
  },
});
</script>
