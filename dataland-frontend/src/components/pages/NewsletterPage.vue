<template>
  <main id="main-content" role="main">
    <section class="newsletter" role="region" aria-labelledby="newsletter-heading">
      <div class="newsletter__wrapper">
        <div class="newsletter__info">
          <h1 id="newsletter-heading" class="newsletter__title">Stay informed</h1>
          <p class="newsletter__text">
            Receive updates on new datasets, framework coverage, events, and platform developments.
          </p>
        </div>
        <div class="newsletter-form">
          <h2 class="newsletter-form__title">Subscribe to our newsletter</h2>
          <form class="newsletter-form__fields" @submit.prevent>
            <label for="newsletter-name" class="newsletter-form__label">Name</label>
            <input id="newsletter-name" type="text" class="newsletter-form__input" placeholder="Your name" disabled />
            <label for="newsletter-email" class="newsletter-form__label">Email</label>
            <input
              id="newsletter-email"
              type="email"
              class="newsletter-form__input"
              placeholder="Your email"
              disabled
            />
            <label for="newsletter-org" class="newsletter-form__label">Organisation</label>
            <input
              id="newsletter-org"
              type="text"
              class="newsletter-form__input"
              placeholder="Your organisation"
              disabled
            />
            <div class="newsletter-form__consent">
              <input id="newsletter-consent" type="checkbox" disabled />
              <label for="newsletter-consent" class="newsletter-form__consent-label">
                I agree to the processing of my data according to the privacy policy
              </label>
            </div>
            <button type="submit" class="newsletter-form__submit" disabled>Subscribe</button>
          </form>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
// Static layout only - form submission logic deferred to backlog
</script>

<style scoped lang="scss">
main {
  margin-top: 132px;
  @media only screen and (max-width: $bp-md) {
    margin-top: 80px;
  }
}

.newsletter {
  padding: 80px 64px;
  background: #111111;
  min-height: calc(100vh - 132px);
  display: flex;
  align-items: center;

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: 45% 55%;
    gap: 48px;
    align-items: start;
    width: 100%;
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  &__title {
    font-size: 2.5rem;
    font-weight: 700;
    margin: 0;
    color: #ffffff;
  }

  &__text {
    font-size: 1.125rem;
    line-height: 1.6;
    margin: 0;
    color: rgba(255, 255, 255, 0.8);
  }
}

.newsletter-form {
  background: rgba(0, 40, 55, 0.45);
  border: 1px solid #2f6fe4;
  border-radius: 24px;
  padding: 40px;

  &__title {
    font-size: 1.5rem;
    color: #ffffff;
    font-weight: 700;
    margin: 0 0 24px;
  }

  &__fields {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  &__label {
    font-size: 0.875rem;
    color: rgba(255, 255, 255, 0.7);
    font-weight: 600;
  }

  &__input {
    background: transparent;
    border: 1px solid #4a4a4a;
    border-radius: 8px;
    padding: 12px 16px;
    color: #9a9a9a;
    font-size: 1rem;
    width: 100%;
    box-sizing: border-box;
  }

  &__consent {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    margin-top: 8px;

    input {
      margin-top: 4px;
    }
  }

  &__consent-label {
    font-size: 0.875rem;
    color: rgba(255, 255, 255, 0.6);
  }

  &__submit {
    background: var(--p-primary-color);
    color: #ffffff;
    border: none;
    border-radius: 8px;
    padding: 12px 24px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    margin-top: 8px;
    width: fit-content;

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .newsletter {
    padding: 40px 16px;

    &__wrapper {
      grid-template-columns: 1fr;
      gap: 32px;
    }

    &__title {
      font-size: 2rem;
    }
  }

  .newsletter-form {
    padding: 24px;
  }
}
</style>
