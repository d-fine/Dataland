<template>
  <div class="flex justify-content-center">
    <PrimeButton
      data-test="contextMenuButton"
      class="p-button-icon p-button-textcolor p-button-rounded"
      type="button"
      icon="pi pi-ellipsis-v"
      @click="toggle"
      aria-haspopup="true"
      aria-controls="overlay_menu"
    />
    <PrimeMenu
      data-test="contextMenuItem"
      class="p-menu-white"
      ref="menu"
      id="overlay_menu"
      :model="menuItems"
      :popup="true"
    />
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import PrimeButton from 'primevue/button';
import PrimeMenu from 'primevue/menu';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'ContextMenuButton',
  methods: {
    /**
     * toggles the menu when event (clicking the button) happens
     * @param event clicking the button
     */
    toggle(event: Event) {
      (this.$refs.menu as InstanceType<typeof PrimeMenu>).toggle(event);
    },
  },
  components: {
    PrimeButton,
    PrimeMenu,
  },
  props: {
    menuItems: Array,
  },
});
</script>
