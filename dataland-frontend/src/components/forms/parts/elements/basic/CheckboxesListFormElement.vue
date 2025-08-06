<template>
  <div class="yes-no-checkboxes">
    <div v-for="option in options" :key="option.value" class="yes-no-option">
      <Checkbox
        v-model="checkboxValue"
        :inputId="`yes-no-${option.value}`"
        :value="option.value"
        @change="updateYesNoValue()"
      />
      <label :for="`yes-no-${option.value}`">{{ option.label }}</label>
    </div>
  </div>
  <FormKit
    type="text"
    :name="name"
    v-model="currentValue"
    :validation="validation"
    :validation-label="validationLabel"
    :validation-messages="validationMessages"
    v-if="!shouldBeIgnoredByFormKit"
    :outer-class="{ 'hidden-input': true, 'formkit-outer': false }"
  />
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import { FormKit } from '@formkit/vue';
import Checkbox from 'primevue/checkbox';

export default defineComponent({
  name: 'CheckboxesListFormElement',
  components: { FormKit, Checkbox },
  data() {
    return {
      shouldBeIgnoredByFormKit: false,
      currentValue: null as string | null,
      checkboxValue: [] as Array<string>,
      yesNoValue: undefined as string | undefined,
    };
  },
  methods: {
    /**
     * Updates the currentValue when the checkboxes value has been changed
     * @param newCheckBoxValue is the new value in the checkbox
     */
    updateCurrentValue(newCheckBoxValue: string) {
      if (newCheckBoxValue && newCheckBoxValue !== '') {
        this.shouldBeIgnoredByFormKit = false;
        this.currentValue = newCheckBoxValue;
      } else {
        this.shouldBeIgnoredByFormKit = !this.validation.includes('is:');
        this.currentValue = null;
      }
    },
    /**
     * updateCurrentValue
     */
    updateYesNoValue() {
      if (!this.checkboxValue.length) {
        this.yesNoValue = undefined;
      }
    },
  },
  watch: {
    currentValue(newVal: string) {
      this.updateCurrentValue(newVal);
      this.$emit('updateCheckboxValue', this.currentValue);
    },

    checkboxValue(newArr: string[]) {
      if (newArr.length > 1) {
        const last = newArr[newArr.length - 1];
        this.checkboxValue = [last];
        this.yesNoValue = last;
      } else if (newArr.length === 1) {
        const [only] = newArr;
        if (this.yesNoValue !== only) {
          this.yesNoValue = only;
        }
      } else {
        this.yesNoValue = undefined;
      }
      this.updateCurrentValue(this.yesNoValue);
    },
  },
  emits: ['updateCheckboxValue'],
  props: {
    name: {
      type: String,
      default: '',
    },
    options: {
      type: Object,
      required: true,
    },
    validation: {
      type: String,
      default: '',
    },
    validationLabel: {
      type: String,
      default: '',
    },
    validationMessages: {
      type: Object as () => { is: string },
    },
  },
});
</script>
<style scoped>
.yes-no-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.yes-no-checkboxes {
  display: flex;
  gap: 7rem;
  align-items: center;
}

.yes-no-checkboxes input[type='checkbox']:hover {
  /* pointer cursor on the box itself */
  cursor: pointer;
}

.yes-no-checkboxes label {
  /* smooth transition if you like */
  transition: background-color 0.2s ease;
}

.yes-no-checkboxes label:hover {
  /* pointer + background on hover */
  cursor: pointer;
  background-color: rgba(0, 0, 0, 0.05);
}
</style>
