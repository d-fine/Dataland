<template>
  <section class="product-intro" role="region" aria-label="Product introduction">
    <div class="product-intro__wrapper">
      <h1 class="product-intro__headline">
        Structured sustainability data. Procured on demand. Quality-assured. Traceable to the original source.
      </h1>
    </div>
  </section>
</template>

<script setup lang="ts">
// No logic needed - static content only
</script>

<style scoped lang="scss">
.product-intro {
  padding: 80px 64px;

  &__wrapper {
    max-width: 900px;
    margin: 0 auto;
    text-align: center;
  }

  &__headline {
    font-size: 2rem;
    font-weight: 700;
    line-height: 1.3;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }
}

@media only screen and (max-width: $bp-md) {
  .product-intro {
    padding: 40px 16px;

    &__headline {
      font-size: 1.5rem;
    }
  }
}
</style>
