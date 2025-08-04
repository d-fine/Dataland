<template>
  <div class="carousel-container">
    <h2>Stay ahead by creating and monitoring Portfolios</h2>

    <Carousel
      :value="carouselItems"
      :numVisible="1"
      :numScroll="1"
      :responsiveOptions="responsiveOptions"
      :circular="true"
      :autoplayInterval="3000"
      class="custom-carousel"
    >
      <template #item="slotProps">
        <div class="carousel-item">
          <div class="item-content">
            <img
              :src="slotProps.data.image"
              :alt="slotProps.data.title"
              class="item-image"
            />
            <div class="item-info">
              <h3>{{ slotProps.data.title }}</h3>
              <p>{{ slotProps.data.description }}</p>
              <span class="item-price">{{ slotProps.data.price }}</span>
            </div>
          </div>
        </div>
      </template>
    </Carousel>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Carousel from 'primevue/carousel';

const carouselItems = ref([
  {
    id: 1,
    title: 'Portfolio Creation',
    description: 'Create and edit Portfolios with our powerful and intuitive interface.',
    image: 'static/portfolioFeatures/EditPortfolio.png'
  },
  {
    id: 2,
    title: 'Portfolio Download',
    description: 'Download latest available ESG data for your Portfolios, containing companies and frameworks that interest you.',
    image: 'static/portfolioFeatures/DownloadPortfolio.png'
  },
  {
    id: 3,
    title: 'Portfolio Monitoring',
    description: 'Monitor companies in your portfolios with given reporting periods and frameworks.',
    image: 'static/portfolioFeatures/MonitorPortfolio.png'
  },
]);

// Responsive Optionen für verschiedene Bildschirmgrößen
const responsiveOptions = ref([
  {
    breakpoint: '9999px',
    numVisible: 1,
    numScroll: 1
  }
]);
</script>

<style scoped lang="scss">
.carousel-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 1rem;
}

.custom-carousel {
  margin: 2rem 0;
}

.carousel-item {
  padding: 1rem;
  height: 100%;
}

.item-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.item-content:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.item-image {
  width: 100%;
  height: auto;
  max-height: 300px; // oder was du willst
  object-fit: contain;
}

.item-info {
  padding: 1.5rem;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.item-info h3 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}

.item-info p {
  margin: 0 0 1rem 0;
  color: #4a5568;
  line-height: 1.5;
  flex-grow: 1;
}

.item-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #3182ce;
  margin-top: auto;
}

/* PrimeVue Carousel Anpassungen */
:deep(.p-carousel-prev),
:deep(.p-carousel-next) {
  background: rgba(255, 255, 255, 0.9);
  color: #374151;
  border: none;
  border-radius: 50%;
  width: 3rem;
  height: 3rem;
  margin: 0.5rem;
}

:deep(.p-carousel-prev:hover),
:deep(.p-carousel-next:hover) {
  background: white;
  color: #1f2937;
}

:deep(.p-carousel-indicators) {
  padding: 1rem 0;
}

:deep(.p-carousel-indicator button) {
  background: #d1d5db;
  border: none;
  border-radius: 50%;
  width: 12px;
  height: 12px;
  margin: 0 0.25rem;
}

:deep(.p-carousel-indicator.p-highlight button) {
  background: #3b82f6;
}
</style>