<template>
  <div class="static-content">
    <main role="main" class="w-12 flex justify-content-center">
      <div class="lg:w-6 md:w-10 sm:w-10 w-12 text-left">
        <slot />
      </div>
    </main>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'LegalPage',
});
</script>

<style lang="scss" scoped>
.static-content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 100vh;
}

main {
  margin-top: 122px;
  margin-bottom: 52px;
  @media only screen and (max-width: 768px) {
    margin-top: 82px;
  }
}

.w-10 {
  width: 10vw;
}
</style>
