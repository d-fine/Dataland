<template>
  <UploadFormSubcategoryHeader label="Activity Name" description="Name of the activity." />
  <div class="form-field next-to-each-other">
    <p>
      <b> {{ selectedActivities ? selectedActivities.name : "" }}</b>
    </p>

    <FormKit type="text" name="activityName" v-model="selectedActivityValue" disabled="true" />

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
              :modelValue="selectedActivities"
              :inputId="slotProps.node.reference"
              name="selectedActivities"
              :value="slotProps.node"
              @update:modelValue="newActivitieSelected($event)"
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
    allActivities: activityTree,
    selectedActivityValue: "",
  }),
  computed: {
    selectedActivities() {
      for (const activities of this.allActivities) {
        if (activities && activities.children?.length) {
          for (const activitie of activities.children) {
            if (activitie.value === this.selectedActivityValue) {
              return activitie;
            }
          }
        }
      }
      return {};
    },

    NaceCodesForActivities() {
      if (this.selectedActivities?.nace_codes) {
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
     * Close the Tree Overlay and set selectedActivityValue.
     * @param event onchange
     */
    newActivitieSelected(event: Event) {
      this.overlayPanel?.hide();
      this.selectedActivityValue = event.value;
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
