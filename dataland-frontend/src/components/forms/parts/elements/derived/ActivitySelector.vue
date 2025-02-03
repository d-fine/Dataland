<template>
  <UploadFormSubcategoryHeader label="Activity Name" description="Name of the activity." />
  <div class="form-field next-to-each-other">
    <p v-if="selectedActivities?.name">
      <b> {{ selectedActivities.name }}</b>
    </p>

    <FormKit type="hidden" name="activityName" v-model="selectedActivityValue" />

    <PrimeButton
      data-test="dataTestChooseActivityButton"
      :label="selectedActivities?.name ? 'Change Activity' : 'Choose Activity'"
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
              @update:modelValue="newActivitySelected($event)"
            />
            <label :for="slotProps.node.key" class="ml-2">{{ slotProps.node.name }}</label>
          </span>
        </template>
      </Tree>
    </div>
  </OverlayPanel>

  <div class="my-4">
    <MultiSelectFormField
      ref="multiSelectFormFieldRef"
      dataTest="selectNaceCodes"
      name="naceCodes"
      :emptyMessage="`No NACE code available for ${selectedActivities?.name} Activity`"
      validation-label="NACE codes for Activity"
      description="The NACE codes associated with this activity"
      label="NACE codes"
      :placeholder="
        selectedActivityValue
          ? 'Choose NACE codes for Activity'
          : 'Please select an activity before selecting NACE code'
      "
      :options="naceCodesForActivities"
      inputClass="long"
    />
  </div>
</template>

<script lang="ts">
import Tree from 'primevue/tree';
import OverlayPanel from 'primevue/overlaypanel';
import { defineComponent, ref } from 'vue';
import RadioButton from 'primevue/radiobutton';
import PrimeButton from 'primevue/button';
import { type ActivityNode, activityTree } from '@/components/forms/parts/elements/derived/ActivityTree';
import MultiSelectFormField from '@/components/forms/parts/fields/MultiSelectFormField.vue';
import UploadFormSubcategoryHeader from '@/components/forms/parts/elements/basic/UploadFormSubcategoryHeader.vue';
import { convertNace } from '@/utils/NaceCodeConverter';
import { type DropdownOption } from '@/utils/PremadeDropdownDatasets';

export default defineComponent({
  name: 'ActivitySelector',
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
    selectedActivityValue: '',
  }),
  computed: {
    selectedActivities(): ActivityNode | undefined {
      for (const activities of this.allActivities) {
        if (activities?.children?.length) {
          for (const activity of activities.children) {
            if (activity.value === this.selectedActivityValue) {
              return activity as ActivityNode;
            }
          }
        }
      }
      return undefined;
    },

    naceCodesForActivities() {
      if (this.selectedActivities?.naceCodes) {
        //@ts-ignore
        this.$refs.multiSelectFormFieldRef?.$refs.multiSelectFormElementRef.clearSelections();
        return (this.selectedActivities.naceCodes).map((naceCode: string) => {
          const naceCodeWithoutLetter: string = naceCode.substring(1);
          const convertedNaceCode = convertNace(
            naceCodeWithoutLetter.length === 1 ? `0${naceCodeWithoutLetter}` : naceCodeWithoutLetter
          );

          return { label: convertedNaceCode, value: naceCode };
        }) as Array<DropdownOption>;
      } else {
        //@ts-ignore
        this.$refs.multiSelectFormFieldRef?.$refs.multiSelectFormElementRef.clearSelections();
        return [];
      }
    },
  },
  methods: {
    /**
     * Close the Tree Overlay and set selectedActivityValue.
     * @param activity activity value from selected NACE codes
     */
    newActivitySelected(activity: ActivityNode) {
      this.overlayPanel?.hide();
      this.selectedActivityValue = activity.value as string;
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
