<template>
  <div class="flex justify-content-center">
    <Button class="p-button-icon p-button-rounded" type="button" icon="pi pi-ellipsis-v" @click="toggle"
            aria-haspopup="true"
            aria-controls="overlay_menu"/>
    <Menu class="p-menu-white" ref="menu" id="overlay_menu" :model="menuItems" :popup="true"/>
  </div>
</template>

<script lang="ts">

import Button from "primevue/button";
import Menu from "primevue/menu";

export default {
  name: "ContextMenuButton",
  methods: {
    toggle(event: Event) {
      this.$refs.menu.toggle(event)
    },
  },
  components: {
    Button, Menu
  },
  props: {
    menuItems: Array,
  }
}
</script>
