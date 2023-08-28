<template>
  <UploadFormSubcategoryHeader label="Activity Name" description="Name of the activity." />
  <div class="form-field next-to-each-other">
    <p>
      <b> {{ selectedActivities ? selectedActivities.name : "" }}</b>
    </p>

    <FormKit
      type="hidden"
      name="activityName"
      :modelValue="selectedActivities ? selectedActivities.value : ''"
      disabled="true"
    />

    <PrimeButton
      data-test="dataTestChooseActivityButton"
      :label="selectedActivities ? 'Change Activity' : 'Choose Activity'"
      class="p-button-text p-0 m-0"
      :icon="selectedActivities ? 'pi pi-pencil' : 'pi pi-list'"
      @focus="inputFocused"
    />
  </div>

  <OverlayPanel ref="overlayPanel" data-test="activityOverlayPanel">
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
              @change="newActivitieSelected"
            />
            <label :for="slotProps.node.key" class="ml-2">{{ slotProps.node.name }}</label>
          </span>
        </template>
      </Tree>
    </div>
  </OverlayPanel>

  <div class="my-4">
    <MultiSelectFormField
      dataTest="selectNaceCodes"
      name="naceCodes"
      validation="required"
      validation-label="Nace Codes for Activity"
      description="The NACE codes associated with this activity"
      label="Nace Codes"
      placeholder="Chose Nace Codes for Activity"
      :options="NaceCodesForActivities"
      innerClass="long"
    />
  </div>
</template>

<script lang="ts">
import Tree from "primevue/tree";
import OverlayPanel from "primevue/overlaypanel";
import { defineComponent, ref } from "vue";
import RadioButton from "primevue/radiobutton";
import PrimeButton from "primevue/button";
import { activityTree } from "@/components/forms/parts/elements/derived/ActivityTree";
import MultiSelectFormField from "@/components/forms/parts/fields/MultiSelectFormField.vue";
import UploadFormSubcategoryHeader from "@/components/forms/parts/elements/basic/UploadFormSubcategoryHeader.vue";
import { convertNace } from "@/utils/NaceCodeConverter";

export default defineComponent({
  name: "ActivitySelector",
  components: {
    UploadFormSubcategoryHeader,
    MultiSelectFormField,
    PrimeButton,
    Tree,
    OverlayPanel,
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
        return (this.selectedActivities.nace_codes as string).split(", ").map((naceCode: string) => {
          const naceCodeWithoutLetter = naceCode.substring(1);
          const convertedNaceCode = convertNace(
            naceCodeWithoutLetter.length === 1 ? `0${naceCodeWithoutLetter}` : naceCodeWithoutLetter,
          );

          return { label: convertedNaceCode, value: naceCode };
        });
      } else {
        return [];
      }
    },
  },
  methods: {
    /**
     * Close the Tree Overlay.
     */
    newActivitieSelected() {
      this.overlayPanel?.hide();
    },
    /**
     * Opens the Tree Overlay.
     * @param event the onclick event
     */
    inputFocused(event: Event) {
      this.overlayPanel?.show(event);
    },
  },
});
</script>
