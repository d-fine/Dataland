<template>
  <MultiSelect
    v-model="selectedItemsBind"
    :options="availableItems"
    :filter="true"
    :showToggleAll="false"
    panelClass="d-framework-data-search-dropdown"
    optionLabel="displayName"
    @before-show="this.overlayVisible = true"
    @before-hide="this.overlayVisible = false"
  >
    <template #indicator>
      <div :class="selectionButtonClasses">
        <div class="selection-button-content flex align-items-center">
          <template v-if="this.modelValue.length === 1">
            {{ modelValue[0].displayName }}
          </template>
          <template v-else>{{ filterName }}</template>
          <span v-if="this.modelValue.length > 0" class="p-badge p-component p-badge-no-gutter ml-1">
            {{ modelValue.length }}</span
          >
          <svg class="mt-1 ml-2" xmlns="http://www.w3.org/2000/svg" width="10" height="5" xml:space="preserve">
            <polygon points="0,0 5,5 10,0" fill="currentColor" />
          </svg>
        </div>
      </div>
    </template>
  </MultiSelect>
</template>

<script lang="ts">
import MultiSelect from "primevue/multiselect";
import { defineComponent } from "vue";

export interface SelectableItem {
  displayName: string;
}

export default defineComponent({
  name: "FrameworkDataSearchDropdownFilter",
  components: { MultiSelect },
  emits: ["update:modelValue"],
  props: {
    availableItems: {
      type: Array as () => Array<SelectableItem>,
      default: () => [],
    },
    modelValue: {
      type: Array as () => Array<SelectableItem>,
      default: () => [],
    },
    filterName: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      overlayVisible: false,
    };
  },
  computed: {
    selectionButtonClasses(): Array<String> {
      const classes = ["selection-button"];
      if (this.overlayVisible) {
        classes.push("overlayVisible");
      } else if (this.modelValue.length > 0) {
        classes.push("filterActive");
      }
      return classes;
    },
    selectedItemsBind: {
      get(): Array<SelectableItem> {
        return this.modelValue;
      },
      set(newValue: Array<SelectableItem>) {
        this.$emit("update:modelValue", newValue);
      },
    },
  },
});
</script>

<style lang="scss">
// Global style is required to overwrite styles of the panel
.d-framework-data-search-dropdown {
  .p-multiselect-item {
    color: black !important;
    border: none !important;

    &.p-highlight {
      color: #e67f3f !important;
      background: #fdefe6 !important;
    }
  }
  .p-multiselect-close {
    display: none;
  }
}
</style>

<style lang="scss" scoped>
$dropdown-button-primary-color: #5a4f36;
:deep(.p-multiselect-label-container) {
  display: none;
}

:deep(.p-badge) {
  background: #fff;
  color: $dropdown-button-primary-color;
}

:deep(.p-multiselect) {
  background: none;
  box-shadow: none !important;
}

:deep(.p-multiselect-trigger) {
  width: auto !important;
}

:deep(.selection-button) {
  background: white;
  color: $dropdown-button-primary-color;
  border: 2px solid $dropdown-button-primary-color;
  border-radius: 8px;
  .selection-button-content {
    margin: 0.5rem 1rem;
  }

  &.overlayVisible {
    background: #e0dfde;
  }

  &.filterActive {
    background: $dropdown-button-primary-color;
    color: white;
  }
}
</style>
