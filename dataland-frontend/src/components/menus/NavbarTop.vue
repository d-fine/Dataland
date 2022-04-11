<template>

  <Menubar :model="items"/>

</template>

<script>

import Menubar from 'primevue/menubar';


export default {
  name: "NavbarTop",
  components: {Menubar},
  data() {
    return {
      items: [
        {
          label: 'Home',
          to: '/',
          icon: 'pi pi-home',
        },
        {
          label: 'Data Upload',
          to: '/upload',
          icon: 'pi pi-upload',
        },
        {
          label: 'Data Search',
          to: '/search',
          icon: 'pi pi-search',
        },
        {
          label: 'EU Taxonomy Search',
          to: '/searchtaxonomy',
          icon: 'pi pi-euro',
        }
      ]
    }
  }
}
</script>