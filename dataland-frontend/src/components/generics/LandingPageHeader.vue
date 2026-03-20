<template>
  <header class="header" role="banner">
    <a href="#main-content" class="skip-link">Skip to content</a>
    <div class="header__inner">
      <div class="header__logo">
        <router-link to="/" aria-label="Go to the Landing Page">
          <img src="/static/logos/gfx_logo_dataland_orange_S.svg" alt="Dataland banner logo" />
        </router-link>
      </div>

      <nav class="header__nav" :class="{ 'header__nav--open': menuOpen }" aria-label="Main navigation">
        <!-- Product dropdown -->
        <div class="header__dropdown" ref="productDropdownRef">
          <button
            class="header__dropdown-trigger"
            :aria-expanded="productOpen"
            aria-controls="product-menu"
            @click="toggleProduct"
            @keydown="handleTriggerKeydown($event, 'product')"
          >
            Product
            <i class="pi pi-chevron-down header__dropdown-icon" aria-hidden="true" />
          </button>
          <div
            v-if="productOpen"
            id="product-menu"
            class="header__dropdown-menu"
            role="menu"
            @keydown="handleMenuKeydown($event, 'product')"
          >
            <router-link
              v-for="item in PRODUCT_MENU_ITEMS"
              :key="item.label"
              :to="item.link"
              role="menuitem"
              class="header__dropdown-item"
              @click="closeAll"
            >
              {{ item.label }}
            </router-link>
          </div>
        </div>

        <!-- About dropdown -->
        <div class="header__dropdown" ref="aboutDropdownRef">
          <button
            class="header__dropdown-trigger"
            :aria-expanded="aboutOpen"
            aria-controls="about-menu"
            @click="toggleAbout"
            @keydown="handleTriggerKeydown($event, 'about')"
          >
            About
            <i class="pi pi-chevron-down header__dropdown-icon" aria-hidden="true" />
          </button>
          <div
            v-if="aboutOpen"
            id="about-menu"
            class="header__dropdown-menu"
            role="menu"
            @keydown="handleMenuKeydown($event, 'about')"
          >
            <router-link
              v-for="item in ABOUT_MENU_ITEMS"
              :key="item.label"
              :to="item.link"
              role="menuitem"
              class="header__dropdown-item"
              @click="closeAll"
            >
              {{ item.label }}
            </router-link>
          </div>
        </div>
      </nav>

      <div class="header__auth" :class="{ 'header__auth--visible': menuOpen }">
        <AuthSection :is-landing-page="true" />
      </div>

      <button
        class="header__hamburger"
        :aria-expanded="menuOpen"
        aria-controls="mobile-menu"
        aria-label="Toggle navigation menu"
        @click="menuOpen = !menuOpen"
      >
        <i :class="menuOpen ? 'pi pi-times' : 'pi pi-bars'" />
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import AuthSection from '@/components/resources/landingPage/AuthSection.vue';
import { ref, onMounted, onUnmounted } from 'vue';

interface MenuItem {
  label: string;
  link: string;
}

const PRODUCT_MENU_ITEMS: MenuItem[] = [
  { label: 'How it works', link: '/product#how-it-works' },
  { label: 'Features', link: '/product#features' },
  { label: 'Frameworks', link: '/product#frameworks' },
  { label: 'Use Cases', link: '/product#use-cases' },
  { label: 'Customer Stories', link: '/product#customer-stories' },
  { label: 'Membership & Pricing', link: '/product#membership-pricing' },
  { label: 'Documentation', link: '/product#documentation' },
];

const ABOUT_MENU_ITEMS: MenuItem[] = [
  { label: 'Company', link: '/about#company' },
  { label: 'Partners', link: '/about#partners' },
  { label: 'News and Insights', link: '/about#updates' },
  { label: 'Contact', link: '/about#contact' },
];

const menuOpen = ref(false);
const productOpen = ref(false);
const aboutOpen = ref(false);
const productDropdownRef = ref<HTMLElement | null>(null);
const aboutDropdownRef = ref<HTMLElement | null>(null);

/**
 * Toggles the Product dropdown menu open/closed and closes the About dropdown
 */
function toggleProduct(): void {
  productOpen.value = !productOpen.value;
  aboutOpen.value = false;
}

/**
 * Toggles the About dropdown menu open/closed and closes the Product dropdown
 */
function toggleAbout(): void {
  aboutOpen.value = !aboutOpen.value;
  productOpen.value = false;
}

/**
 * Closes all dropdown menus and the mobile navigation overlay
 */
function closeAll(): void {
  productOpen.value = false;
  aboutOpen.value = false;
  menuOpen.value = false;
}

/**
 * Closes dropdown menus when clicking outside of them
 * @param event the mouse event
 */
function handleClickOutside(event: MouseEvent): void {
  const target = event.target as Node;
  if (productDropdownRef.value && !productDropdownRef.value.contains(target)) {
    productOpen.value = false;
  }
  if (aboutDropdownRef.value && !aboutDropdownRef.value.contains(target)) {
    aboutOpen.value = false;
  }
}

/**
 * Closes all menus when the Escape key is pressed
 * @param event the keyboard event
 */
function handleEscape(event: KeyboardEvent): void {
  if (event.key === 'Escape') {
    closeAll();
  }
}

/**
 * Handles keyboard interaction on dropdown trigger buttons
 * @param event the keyboard event
 * @param menu which dropdown to control
 */
function handleTriggerKeydown(event: KeyboardEvent, menu: 'product' | 'about'): void {
  if (event.key === 'ArrowDown' || event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    if (menu === 'product') {
      productOpen.value = true;
    } else {
      aboutOpen.value = true;
    }
    requestAnimationFrame(() => {
      const menuEl =
        menu === 'product' ? document.getElementById('product-menu') : document.getElementById('about-menu');
      const firstItem = menuEl?.querySelector('[role="menuitem"]') as HTMLElement | null;
      firstItem?.focus();
    });
  }
}

/**
 * Handles keyboard navigation within an open dropdown menu
 * @param event the keyboard event
 * @param menu which dropdown to control
 */
function handleMenuKeydown(event: KeyboardEvent, menu: 'product' | 'about'): void {
  const menuEl = menu === 'product' ? document.getElementById('product-menu') : document.getElementById('about-menu');
  if (!menuEl) return;

  const items = Array.from(menuEl.querySelectorAll('[role="menuitem"]'));
  const currentIndex = items.indexOf(event.target as HTMLElement);

  if (event.key === 'ArrowDown') {
    event.preventDefault();
    const nextIndex = (currentIndex + 1) % items.length;
    items[nextIndex].focus();
  } else if (event.key === 'ArrowUp') {
    event.preventDefault();
    const prevIndex = (currentIndex - 1 + items.length) % items.length;
    items[prevIndex].focus();
  } else if (event.key === 'Escape') {
    event.preventDefault();
    if (menu === 'product') {
      productOpen.value = false;
    } else {
      aboutOpen.value = false;
    }
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
  document.addEventListener('keydown', handleEscape);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
  document.removeEventListener('keydown', handleEscape);
});
</script>

<style lang="scss" scoped>
.skip-link {
  position: absolute;
  left: -9999px;
  z-index: 999;
  padding: 0.5rem 1rem;
  background: var(--p-primary-color);
  color: #fff;
  font-weight: 600;
  text-decoration: none;

  &:focus {
    left: 1rem;
    top: 1rem;
  }
}

.header {
  height: 72px;
  background: var(--p-surface-0, #f7f7f5);
  border-bottom: 1px solid var(--p-surface-200, #e6e6e6);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0 32px;

  &__inner {
    max-width: 1440px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
  }

  &__logo {
    flex-shrink: 0;

    img {
      width: 143px;
      height: auto;
      display: block;
    }
  }

  &__nav {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-grow: 1;
    justify-content: center;
  }

  &__dropdown {
    position: relative;
  }

  &__dropdown-trigger {
    display: flex;
    align-items: center;
    gap: 4px;
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1rem;
    font-weight: 600;
    color: var(--p-text-color, #1b1b1b);
    padding: 8px 16px;
    border-radius: 8px;

    &:hover {
      color: var(--p-primary-color);
    }

    &:focus-visible {
      outline: 2px solid var(--p-primary-color);
      outline-offset: 2px;
    }
  }

  &__dropdown-icon {
    font-size: 0.75rem;
  }

  &__dropdown-menu {
    position: absolute;
    top: 100%;
    left: 0;
    min-width: 240px;
    background: var(--p-surface-0, #ffffff);
    border: 1px solid var(--p-surface-200, #e6e6e6);
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    padding: 8px 0;
    z-index: 101;
    display: flex;
    flex-direction: column;
  }

  &__dropdown-item {
    display: block;
    padding: 10px 16px;
    font-size: 0.875rem;
    color: var(--p-text-color, #1b1b1b);
    text-decoration: none;
    font-weight: 500;

    &:hover {
      background: var(--p-surface-100, #f0f0f0);
      color: var(--p-primary-color);
    }

    &:focus-visible {
      outline: 2px solid var(--p-primary-color);
      outline-offset: -2px;
    }
  }

  &__auth {
    flex-shrink: 0;
  }

  &__hamburger {
    display: none;
    align-items: center;
    justify-content: center;
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1.25rem;
    color: var(--p-text-color, #1b1b1b);
    padding: 0.5rem;
  }
}

@media only screen and (max-width: $bp-lg) {
  .header {
    &__inner {
      flex-wrap: wrap;
    }

    &__nav {
      display: none;
      width: 100%;
      flex-direction: column;
      gap: 0;
      padding: 16px 0;
      align-items: stretch;
    }

    &__nav--open {
      display: flex;
      position: fixed;
      top: 64px;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.96);
      backdrop-filter: blur(16px);
      z-index: 100;
      overflow-y: auto;
      padding: 16px;
    }

    &__dropdown-menu {
      position: static;
      box-shadow: none;
      border: none;
      padding: 0 0 0 16px;
    }

    &__auth {
      display: none;
      width: 100%;
      justify-content: center;
      padding: 8px 0;
    }

    &__auth--visible {
      display: flex;
    }

    &__hamburger {
      display: flex;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .header {
    padding: 0 16px;
    height: 64px;

    &__logo img {
      width: 100px;
    }
  }
}
</style>
