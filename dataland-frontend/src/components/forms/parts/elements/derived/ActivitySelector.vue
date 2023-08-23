<template>
  <div class="form-field next-to-each-other">
    <div class="form-field-label">
      <p>Activity:</p>
      <p>
        <b> {{ selectedActivities ? selectedActivities.name : "" }}</b>
      </p>
    </div>

    <PrimeButton
      data-test="dataTestChooseActivityButton"
      :label="selectedActivities ? 'Change Activity' : 'Choose Activity'"
      class="p-button-text p-0 m-0"
      :icon="selectedActivities ? 'pi pi-pencil' : 'pi pi-list'"
      @focus="inputFocused"
    />
  </div>

  <OverlayPanel ref="overlayPanel">
    <div>
      <Tree :value="allActivities" class="w-full md:w-30rem" placeholder="Select Activitie">
        <template #default="slotProps">
          <b>{{ slotProps.node.name }}</b>
        </template>
        <template #child="slotProps">
          <span class="next-to-each-other -ml-5">
            <RadioButton
              v-model="selectedActivities"
              :inputId="slotProps.node.reference"
              name="selectedActivities"
              :value="slotProps.node"
            />
            <label :for="slotProps.node.key" class="ml-2">{{ slotProps.node.name }}</label>
          </span>
        </template>
      </Tree>
    </div>
  </OverlayPanel>

  <div class="mt-2">
    <MultiSelectFormElement
      name="naceCodes"
      validation="required"
      validation-label="Nace Codes for Activity"
      placeholder="Chose Nace Codes for Activity"
      :options="NaceCodesForActivities"
      innerClass="long"
    />
  </div>
</template>

<script lang="ts">
import Tree, { type TreeNode } from "primevue/tree";
import InputText from "primevue/inputtext";
import OverlayPanel from "primevue/overlaypanel";
import { defineComponent, PropType, ref } from "vue";
import RadioButton from "primevue/radiobutton";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import PrimeButton from "primevue/button";
import { activityTree } from "@/components/forms/parts/elements/derived/ActivityTree";
import MultiSelectFormElement from "@/components/forms/parts/elements/basic/MultiSelectFormElement.vue";

export default defineComponent({
  name: "ActivitySelector",
  components: {
    MultiSelectFormElement,
    PrimeButton,
    UploadFormHeader,
    Tree,
    OverlayPanel,
    InputText,
    RadioButton,
  },
  setup() {
    return {
      overlayPanel: ref<OverlayPanel>(),
    };
  },
  data: () => ({
    selectedActivities: null,
    allActivities: activityTree,
  }),
  computed: {
    NaceCodesForActivities() {
      if (this.selectedActivities && this.selectedActivities.nace_codes) {
        return this.selectedActivities.nace_codes.split(", ").map((nace_code: string) => {
          return { label: nace_code, value: nace_code };
        });
      } else {
        return [];
      }
    },
  },
  methods: {
    /**
     * Executed, whenever the search bar input is focused. Opens the Tree Overlay.
     * @param event the onclick event
     */
    inputFocused(event: Event) {
      this.overlayPanel?.show(event);
    },
  },
});
</script>
