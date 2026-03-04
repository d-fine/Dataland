<template>
  <section class="about-trust-pillars" role="region" aria-labelledby="trust-pillars-heading">
    <div class="about-trust-pillars__wrapper">
      <h2 id="trust-pillars-heading" class="about-trust-pillars__heading">Why Trust Dataland</h2>
      <div class="grid">
        <div v-for="pillar in TRUST_PILLARS" :key="pillar.title" class="col-12 md:col-6 xl:col-3">
          <TrustPillarCard :pillar="pillar" />
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { TRUST_PILLARS } from '@/components/resources/aboutPage/aboutContent';
import TrustPillarCard from '@/components/resources/aboutPage/TrustPillarCard.vue';
</script>

<style scoped lang="scss">
.about-trust-pillars {
  padding: 4rem 2rem;
  background: var(--p-surface-0, #ffffff);
  border-bottom: 1px solid var(--p-surface-200, #dadada);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }

  &__heading {
    font-size: 1.75rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }
}

@media only screen and (max-width: 768px) {
  .about-trust-pillars {
    padding: 2.5rem 1rem;
  }
}
</style>