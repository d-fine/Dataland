<template>
  <div data-test="AddressFormElement">
    <Tree :value="allActivities" class="w-full md:w-30rem">
      <template #default="slotProps">
        <b>{{ slotProps.node.label }}</b>
      </template>
      <template #check="slotProps">
        <Checkbox v-model="selectedActivities" :inputId="slotProps.node.key" name="selectedActivities" :value="slotProps.node.data" />
        <label :for="slotProps.node.key" class="ml-2">{{ slotProps.node.label }}</label>
      </template>
    </Tree>
    {{ JSON.stringify(selectedActivities) }}
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import StringBadgeListFormField from "@/components/forms/parts/fields/StringBadgeListFormField.vue";
import Tree from "primevue/tree";
import Checkbox from "primevue/checkbox";

export default defineComponent({
  name: "AlignedActivitiesFormElements",
  components: {
    InputTextFormField,
    FreeTextFormField,
    StringBadgeListFormField,
    Tree,
    Checkbox,
  },
  data: () => ({
    selectedActivities: null,
    allActivities: [
      {
        key: "0",
        label: "Forestry",
        children: [
          {
            key: "0-0",
            label: "Afforestation",
            data: "10",
            type: "check",
          },
          {
            key: "0-1",
            label: "Rehabilitation and restoration of forests, including reforestation and natural forest regeneration after an extreme event",
            data: "11",
            type: "check",
          },
          {
            key: "0-2",
            label: "Forest management",
            data: "12",
            type: "check",
          },
          {
            key: "0-3",
            label: "Conservation forestry",
            data: "13",
            type: "check",
          },
        ],
      },
      {
        key: "1",
        label: "Manufacturing",
        children: [
          {
            key: "1-0",
            label: "Manufacture of renewable energy technologies",
            data: "15",
            type: "check",
          },
          {
            key: "1-1",
            label: "Manufacture of equipment for the production and use of hydrogen",
            data: "17",
            type: "url"
          },
          {
            key: "1-2",
            label: "Manufacture of low carbon technologies for transport",
            data: "16",
            type: "check",
          },
          {
            key: "1-3",
            label: "Manufacture of batteries",
            data: "18",
            type: "url"
          },
        ],
      },
      {
        key: "2",
        label: "Environmental protection and restoration activities",
      },
    ],
  }),
});
</script>
