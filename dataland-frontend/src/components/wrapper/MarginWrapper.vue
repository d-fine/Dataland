<template>
  <div :class="'grid ' + bgClass">
    <div class="col-12 pl-4 pr-4 pb-0">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: "MarginWrapper",
  props: {
    bgClass: {
      type: String,
      default: "",
    },
  },
};
</script>
