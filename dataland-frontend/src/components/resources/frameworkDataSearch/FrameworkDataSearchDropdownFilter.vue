<template>
  <MultiSelect
    v-model="selectedItemsBind"
    ref="multiselect"
    :options="availableItems"
    :filter="true"
    :showToggleAll="false"
    :filterPlaceholder="filterPlaceholder"
    panelClass="d-framework-data-search-dropdown"
    optionLabel="displayName"
    optionDisabled="disabled"
    @before-show="this.overlayVisible = true"
    @before-hide="this.overlayVisible = false"
  >
    <template #indicator>
      <div :class="selectionButtonClasses">
        <div class="selection-button-content" :id="filterId">
          <template v-if="this.modelValue.length === 1">
            {{ modelValue[0].displayName }}
          </template>
          <template v-else>{{ filterName }}</template>
          <span v-if="this.modelValue.length > 0" class="p-badge p-component p-badge-no-gutter ml-1">
            {{ modelValue.length }}</span
          >
          <svg class="ml-2" xmlns="http://www.w3.org/2000/svg" width="10" height="7" xml:space="preserve">
            <polygon points="0,0 5,5 10,0" fill="currentColor" />
          </svg>
        </div>
      </div>
    </template>
  </MultiSelect>
</template>

<script lang="ts">
import MultiSelect from "primevue/multiselect";
import { defineComponent, ref } from "vue";
import { SelectableItem } from "@/utils/FrameworkDataSearchDropDownFilterTypes";

export default defineComponent({
  setup() {
    return {
      multiselect: ref(),
    };
  },
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
    filterId: {
      type: String,
      default: "",
    },
    filterPlaceholder: {
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
    selectionButtonClasses(): Array<string> {
      const classes = ["selection-button", "flex", "flex-row", "align-items-center"];
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
    font-weight: 400;

    &.p-highlight {
      color: #e67f3f !important;
      background: #fdefe6 !important;
    }

    &.p-disabled {
      color: #958d7c !important;

      &::after {
        content: "(Not available)";
        margin-left: 0.2rem;
        font-weight: 500;
        color: black !important;
      }
    }
  }
  .p-multiselect-close {
    display: none;
  }
}
</style>

<style lang="scss" scoped>
:deep(.p-multiselect-label-container) {
  display: none;
}

:deep(.p-badge) {
  background: #fff;
  color: #5a4f36;
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
  color: #5a4f36;
  border: 2px solid #5a4f36;
  border-radius: 8px;
  height: 2.5rem;
  .selection-button-content {
    margin: 0.5rem 1rem;
  }

  &.overlayVisible {
    background: #e0dfde;
  }

  &.filterActive {
    background: #5a4f36;
    color: white;
  }
}
</style>
