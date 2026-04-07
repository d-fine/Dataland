<template>
  <Teleport to="body">
    <div
      v-if="isOpen"
      class="mobile-nav-overlay"
      @click="close"
    />
    <nav
      v-show="isOpen"
      ref="navRef"
      class="mobile-nav"
      aria-label="Mobile navigation"
      role="dialog"
      aria-modal="true"
    >
      <div class="mobile-nav__header">
        <span class="mobile-nav__title">Menu</span>
        <button
          ref="closeButtonRef"
          type="button"
          class="mobile-nav__close"
          aria-label="Close menu"
          @click="close"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>

      <div class="mobile-nav__links">
        <a
          v-for="link in navLinks"
          :key="link.href"
          :href="link.href"
          :class="['mobile-nav__link', { 'mobile-nav__link--active': isActive(link.href) }]"
          :aria-current="isActive(link.href) ? 'page' : undefined"
          @click="close"
        >{{ link.label }}</a>
        <a href="/login" data-test="login-dataland-button" v-if="!isAuthenticated" class="mobile-nav__link" @click="close">Login</a>
        <a href="/register" v-if="!isAuthenticated" class="mobile-nav__cta" @click="close">Try it free</a>
        <a href="/companies" v-if="isAuthenticated" class="mobile-nav__link mobile-nav__back-to-platform" @click="close">
          Back to platform
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M5 12h14M12 5l7 7-7 7"/>
          </svg>
        </a>
      </div>
    </nav>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from 'vue';

const props = defineProps<{
  currentPath?: string;
}>();

const navLinks = [
  { href: '/', label: 'Home' },
  { href: '/product', label: 'Product' },
  { href: '/about', label: 'About' },
  { href: '/community', label: 'Community' },
];

function isActive(href: string): boolean {
  const path = props.currentPath ?? window.location.pathname;
  if (href === '/') return path === '/';
  return path.startsWith(href);
}

const isOpen = ref(false);
const closeButtonRef = ref<HTMLButtonElement | null>(null);
const navRef = ref<HTMLElement | null>(null);
const isAuthenticated = ref(false);

function close(): void {
  isOpen.value = false;
  // Return focus to the hamburger button
  const toggle = document.getElementById('mobile-menu-toggle');
  nextTick(() => {
    toggle?.focus();
  });
}

function handleToggle(): void {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    // Focus the close button when opening
    nextTick(() => {
      closeButtonRef.value?.focus();
    });
  }
}

function handleKeydown(event: KeyboardEvent): void {
  if (event.key === 'Escape' && isOpen.value) {
    close();
  }
}

onMounted(() => {
  isAuthenticated.value = localStorage.getItem('dataland_authenticated') === 'true';
  document.addEventListener('toggle-mobile-nav', handleToggle);
  document.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  document.removeEventListener('toggle-mobile-nav', handleToggle);
  document.removeEventListener('keydown', handleKeydown);
});
</script>

<style scoped>
.mobile-nav-overlay {
  position: fixed;
  inset: 0;
  z-index: 90;
  background: rgba(0, 0, 0, 0.3);
}

.mobile-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: var(--color-surface-0, #ffffff);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 1rem 1.5rem 2rem;
}

.mobile-nav__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.mobile-nav__title {
  font-weight: 600;
  font-size: 1.125rem;
  color: var(--color-text, #1a1a1a);
}

.mobile-nav__close {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  color: var(--color-text, #1a1a1a);
}

.mobile-nav__links {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.mobile-nav__link {
  display: block;
  padding: 0.75rem 0;
  text-decoration: none;
  color: var(--color-text, #1a1a1a);
  font-weight: 500;
  font-size: 1rem;
  border-bottom: 1px solid var(--color-surface-200, #e6e6e6);
}

@media (prefers-reduced-motion: no-preference) {
  .mobile-nav__link {
    transition: color 0.15s;
  }
}

.mobile-nav__link:hover {
  color: var(--color-primary, #ff6813);
}

.mobile-nav__link--active {
  color: var(--color-primary, #ff6813);
}

.mobile-nav__cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 1rem;
  padding: 0.75rem 1.5rem;
  background: var(--color-primary, #ff6813);
  color: white;
  font-weight: 600;
  font-size: 1rem;
  border-radius: 6px;
  text-decoration: none;
  text-align: center;
}

@media (prefers-reduced-motion: no-preference) {
  .mobile-nav__cta {
    transition: background 0.15s;
  }
}

.mobile-nav__back-to-platform {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.mobile-nav__cta:hover {
  background: var(--color-primary-hover, #e55d10);
}
</style>
