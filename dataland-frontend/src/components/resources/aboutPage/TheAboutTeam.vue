<template>
  <section id="team" class="about-team" role="region" aria-labelledby="team-heading">
    <div class="about-team__wrapper">
      <h2 id="team-heading" class="about-team__heading">Leadership team</h2>
      <div class="about-team__grid">
        <div v-for="person in LEADERSHIP_TEAM" :key="person.name" class="about-team__card">
          <div class="about-team__photo-container">
            <img :src="person.imagePath" :alt="person.name" class="about-team__photo" />
          </div>
          <div class="about-team__info">
            <h3 class="about-team__name">{{ person.name }}</h3>
            <p class="about-team__role">{{ person.role }}</p>
            <div class="about-team__links">
              <a :href="person.email" class="about-team__link" aria-label="Send email">
                <i class="pi pi-envelope" aria-hidden="true" />
              </a>
              <a
                :href="person.linkedIn"
                target="_blank"
                rel="noopener noreferrer"
                class="about-team__link"
                aria-label="LinkedIn profile"
              >
                <i class="pi pi-linkedin" aria-hidden="true" />
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { LEADERSHIP_TEAM } from '@/components/resources/aboutPage/aboutContent';
</script>

<style scoped lang="scss">
.about-team {
  padding: 80px 64px;
  background: #0f3a82;

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 40px;
  }

  &__heading {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: #ffffff;
    text-align: center;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 32px;
  }

  &__card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  &__photo-container {
    width: 100%;
    height: 320px;
    border-radius: 12px;
    overflow: hidden;
    background: rgba(255, 255, 255, 0.1);
  }

  &__photo {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &__info {
    text-align: center;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__name {
    font-size: 1.25rem;
    font-weight: 700;
    margin: 0;
    color: #ffffff;
  }

  &__role {
    font-size: 1rem;
    margin: 0;
    color: rgba(255, 255, 255, 0.7);
  }

  &__links {
    display: flex;
    gap: 12px;
    justify-content: center;
    margin-top: 8px;
  }

  &__link {
    color: #ffffff;
    font-size: 1.125rem;
    text-decoration: none;

    &:hover {
      color: var(--p-primary-color);
    }
  }
}

@media only screen and (max-width: $bp-lg) {
  .about-team__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media only screen and (max-width: $bp-md) {
  .about-team {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }

  .about-team__grid {
    grid-template-columns: 1fr;
  }

  .about-team__photo-container {
    height: 280px;
  }
}
</style>
