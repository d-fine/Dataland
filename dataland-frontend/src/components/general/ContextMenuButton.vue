<template>
  <div class="flex justify-content-center">
    <PrimeButton class="p-button-icon p-button-textcolor p-button-rounded " type="button"
                 icon="pi pi-ellipsis-v"
                 @click="toggle"
                 aria-haspopup="true"
                 aria-controls="overlay_menu"/>
    <PrimeMenu class="p-menu-white" ref="menu" id="overlay_menu" :model="menuItems" :popup="true"/>
  </div>
</template>


<script lang="ts">

import PrimeButton from "primevue/button";
import PrimeMenu from "primevue/menu" ;

export default {
  name: "ContextMenuButton",
  methods: {
    toggle(event: Event) {
      this.$refs.menu.toggle(event)
    },
  },
  components: {
    PrimeButton, PrimeMenu
  },
  props: {
    menuItems: Array,
  }
}
</script>
