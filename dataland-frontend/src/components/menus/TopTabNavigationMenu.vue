<template>
  <div class="w-full h-2rem d-top-tab-nav flex justify-content-center align-items-center">
    <div class="d-top-tab-nav-spacer flex-grow-1"></div>
    <template v-for="(tab, index) in tabs" :key="index">
      <div
        :class="index === modelValue ? 'd-top-tab-nav-item d-top-tab-nav-item-active' : 'd-top-tab-nav-item'"
        @click="selectTab(index)"
      >
        {{ tab }}
      </div>
    </template>
    <div class="d-top-tab-nav-spacer flex-grow-1"></div>
  </div>
</template>

<style scoped>

.d-top-tab-nav-item-active {
  cursor: default !important;
  color: #e67f3f !important;
  border: 2px solid #e67f3f !important;
  border-bottom: none !important;
  background: white;
}

.d-top-tab-nav-spacer {
  height: 100%;
  border-bottom: 2px solid #e67f3f;
}

.d-top-tab-nav-item:hover {
  color: #e67f3f !important;
}

.d-top-tab-nav-item {
  transition: color 0.25s ease;
  cursor: pointer;
  padding-left: 1rem;
  padding-right: 1rem;
  color: #958d7c;
  height: 100%;
  border-top: 2px solid transparent;
  border-bottom: 2px solid #e67f3f;
}

.d-top-tab-nav {
  background: #f7f9fa;
}
</style>

<script>
export default {
  name: "TopTabNavigationMenu",
  props: {
    tabs: {
      default: [],
    },
    modelValue: {
      type: Number,
      default: 0,
    },
  },
  methods: {
    selectTab(index) {
      this.$emit("update:modelValue", index);
    },
  },
};
</script>
