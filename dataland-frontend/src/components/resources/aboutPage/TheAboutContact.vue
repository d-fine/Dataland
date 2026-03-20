<template>
  <section id="contact" class="about-contact" role="region" aria-labelledby="contact-heading">
    <div class="about-contact__wrapper">
      <div class="about-contact__info">
        <h2 id="contact-heading" class="about-contact__title">Dataland</h2>
        <address class="about-contact__details">
          <p class="about-contact__company">Dataland GmbH</p>
          <p class="about-contact__address">Am Steinernen Stock 1, 60320 Frankfurt am Main, Deutschland</p>
          <p class="about-contact__phone">
            <a href="tel:+491622631304" class="about-contact__link">+49 1622 63 13 04</a>
          </p>
          <p class="about-contact__email">
            <a href="mailto:info@dataland.com" class="about-contact__link">info@dataland.com</a>
          </p>
        </address>
      </div>
      <div class="contact-form">
        <h3 class="contact-form__title">Request a demo</h3>
        <p class="contact-form__subtitle">We will get back to you within one business day</p>
        <form class="contact-form__fields" @submit.prevent>
          <label for="contact-name" class="contact-form__label">Name</label>
          <input id="contact-name" type="text" class="contact-form__input" placeholder="Your name" disabled />
          <label for="contact-email" class="contact-form__label">Email</label>
          <input id="contact-email" type="email" class="contact-form__input" placeholder="Your email" disabled />
          <label for="contact-message" class="contact-form__label">Message</label>
          <textarea
            id="contact-message"
            class="contact-form__input contact-form__textarea"
            placeholder="Your message"
            disabled
          ></textarea>
          <div class="contact-form__consent">
            <input id="contact-consent" type="checkbox" disabled />
            <label for="contact-consent" class="contact-form__consent-label">
              I agree to the processing of my data according to the privacy policy
            </label>
          </div>
          <button type="submit" class="contact-form__submit" disabled>Send request</button>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
// Static layout only - form submission logic deferred to backlog
</script>

<style scoped lang="scss">
.about-contact {
  padding: 80px 64px;
  background: #111111;

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: 45% 55%;
    gap: 48px;
    align-items: start;
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  &__title {
    font-size: 2.5rem;
    font-weight: 700;
    margin: 0;
    color: #ffffff;
  }

  &__details {
    font-style: normal;
    display: flex;
    flex-direction: column;
    gap: 8px;

    p {
      margin: 0;
      color: rgba(255, 255, 255, 0.8);
      font-size: 1rem;
      line-height: 1.5;
    }
  }

  &__company {
    font-weight: 600;
  }

  &__link {
    color: rgba(255, 255, 255, 0.8);
    text-decoration: none;

    &:hover {
      color: var(--p-primary-color);
    }
  }
}

.contact-form {
  background: rgba(0, 40, 55, 0.45);
  border: 1px solid #2f6fe4;
  border-radius: 24px;
  padding: 40px;

  &__title {
    font-size: 2rem;
    color: #ffffff;
    font-weight: 700;
    margin: 0 0 8px;
  }

  &__subtitle {
    font-size: 1.25rem;
    color: var(--p-surface-200, #dadada);
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

  &__textarea {
    min-height: 120px;
    resize: vertical;
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
  .about-contact {
    padding: 40px 16px;

    &__wrapper {
      grid-template-columns: 1fr;
      gap: 32px;
    }

    &__title {
      font-size: 2rem;
    }
  }

  .contact-form {
    padding: 24px;
  }
}
</style>
