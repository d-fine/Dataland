<template>
  <section class="trustbar" role="region" aria-label="Trusted by">
    <p class="trustbar__label">Trusted by leaders in European financial services</p>
    <div class="trustbar__logos">
      <img v-for="logo in logos" :key="logo.alt" :src="logo.src" :alt="logo.alt" class="trustbar__logo" />
    </div>
  </section>
</template>

<script setup lang="ts">
interface TrustLogo {
  src: string;
  alt: string;
}

const logos: TrustLogo[] = [
  { src: '/static/logos/img_bvi.png', alt: 'BVI' },
  { src: '/static/logos/img_voeb.png', alt: 'VOEB' },
  { src: '/static/logos/img_d-fine.png', alt: 'd-fine' },
  { src: '/static/logos/img_pwc.png', alt: 'PwC' },
  { src: '/static/logos/img_wertestiftung.png', alt: 'Werte-Stiftung' },
  { src: '/static/logos/img_deka.png', alt: 'Deka' },
  { src: '/static/logos/img_Metzler.png', alt: 'Metzler' },
  { src: '/static/logos/img_bayernlb.png', alt: 'BayernLB' },
];
</script>

<style scoped lang="scss">
.trustbar {
  padding: 48px 32px;
  background: var(--p-surface-0, #ffffff);
  text-align: center;
  border-bottom: 1px solid var(--p-surface-200, #dadada);

  &__label {
    font-size: 16px;
    font-weight: 600;
    color: var(--grey-tones-600);
    text-transform: uppercase;
    letter-spacing: 1px;
    margin: 0 0 32px;
  }

  &__logos {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    align-items: center;
    gap: 32px 48px;
    max-width: 1200px;
    margin: 0 auto;
  }

  &__logo {
    height: 40px;
    width: auto;
    filter: grayscale(100%);
    opacity: 0.6;
    transition:
      filter 0.3s,
      opacity 0.3s;

    &:hover {
      filter: grayscale(0%);
      opacity: 1;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .trustbar {
    padding: 32px 16px;

    &__label {
      font-size: 14px;
    }

    &__logos {
      gap: 24px 32px;
    }

    &__logo {
      height: 32px;
    }
  }
}
</style>
