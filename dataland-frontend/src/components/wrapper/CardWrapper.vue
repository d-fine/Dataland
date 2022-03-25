<template>
  <div class="grid">
      <div class="col-12">
          <Card>
          <slot></slot>
          </Card>
      </div>
  </div>
</template>

<script>
import Card from 'primevue/card';
export default {
  name: "CardWrapper",
  components: {Card},
}
</script>
