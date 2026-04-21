<template>
  <footer class="footer" role="contentinfo" data-test="dataland footer">
    <div class="footer__top">
      <div class="footer__col footer__col--contact">
        <a href="/" class="footer__logo-link" aria-label="Dataland home">
          <img
              src="/static/logos/logo_dataland_orange.svg"
              alt="Dataland"
              width="240"
              height="55"
              loading="lazy"
              class="footer__logo-img"
          />
        </a>
        <a
            href="https://wertestiftung.org/"
            target="_blank"
            rel="noopener noreferrer"
            class="footer__logo-link"
            aria-label="Visit Werte-Stiftung"
        >
          <img
              src="/static/logos/a_werte-stiftung_company.svg"
              alt="Werte-Stiftung Company"
              width="240"
              height="24"
              loading="lazy"
              class="footer__logo-img"
          />
        </a>
        <div class="footer__contact-block">
          <div class="footer__contact-address">
            <p class="footer__company-name">{{ CONTACT_ADDRESS.company }}</p>
            <div class="footer__address-row">
              <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.7"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  aria-hidden="true"
                  class="footer__icon footer__icon--location"
              >
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
              <div class="footer__address-lines">
                <p class="footer__address-line">{{ CONTACT_ADDRESS.street }}</p>
                <p class="footer__address-line">{{ CONTACT_ADDRESS.city }}, {{ CONTACT_ADDRESS.country }}</p>
              </div>
            </div>
          </div>
          <a :href="CONTACT_PHONE" class="footer__contact-link">
            <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
                class="footer__icon"
            >
              <path
                  d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"
              ></path>
            </svg>
            <span>{{ CONTACT_PHONE_DISPLAY }}</span>
          </a>
          <a :href="CONTACT_EMAIL" class="footer__contact-link">
            <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
                class="footer__icon"
            >
              <rect x="2" y="4" width="20" height="16" rx="2"></rect>
              <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"></path>
            </svg>
            <span>{{ CONTACT_EMAIL_DISPLAY }}</span>
          </a>
        </div>
      </div>
      <div class="footer__col">
        <h3 class="footer__col-heading">About</h3>
        <ul class="footer__link-list">
          <li v-for="section in aboutSections" :key="section.href">
            <a :href="section.href" class="footer__list-link">
              {{ section.label }}
            </a>
          </li>
        </ul>
      </div>
      <div class="footer__col">
        <h3 class="footer__col-heading">Product</h3>
        <ul class="footer__link-list">
          <li v-for="section in productSections" :key="section.href">
            <a :href="section.href" class="footer__list-link">
              {{ section.label }}
            </a>
          </li>
        </ul>
      </div>
      <div class="footer__col">
        <h3 class="footer__col-heading">Connect and explore</h3>
        <ul class="footer__link-list">
          <li>
            <a
                href="https://www.linkedin.com/company/dataland-gmbh/"
                target="_blank"
                rel="noopener noreferrer"
                class="footer__list-link footer__list-link--inline"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <path
                    d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433a2.062 2.062 0 01-2.063-2.065 2.064 2.064 0 112.063 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"
                ></path>
              </svg>
              <span>LinkedIn</span>
            </a>
          </li>
          <li>
            <a :href="CONTACT_INQUIRY_MAILTO" class="footer__list-link footer__list-link--inline">
              <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  aria-hidden="true"
              >
                <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"></path>
              </svg>
              <span>Get in touch</span>
            </a>
          </li>
          <li>
            <a href="/login" class="footer__list-link footer__list-link--inline">
              <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  aria-hidden="true"
              >
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                <polyline points="10 17 15 12 10 7"></polyline>
                <line x1="15" y1="12" x2="3" y2="12"></line>
              </svg>
              <span>Login</span>
            </a>
          </li>
          <li>
            <a href="/register" class="footer__list-link footer__list-link--inline">
              <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  aria-hidden="true"
              >
                <path d="M12 5v14"></path>
                <path d="M5 12h14"></path>
              </svg>
              <span>Try it free</span>
            </a>
          </li>
        </ul>
      </div>
    </div>
    <div class="footer__bottom">
      <div class="footer__bottom-inner">
        <div class="footer__bottom-left">
          <a href="/legal" class="footer__bottom-link">Legal</a>
          <a href="/imprint" class="footer__bottom-link">Imprint</a>
          <a href="/dataprivacy" class="footer__bottom-link">Data Privacy</a>
        </div>
        <div class="footer__bottom-right">
          <span class="footer__copyright">&copy; {{ currentYear }} Dataland</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue';

defineOptions({ name: 'TheFooter' });

const currentYear = computed(() => new Date().getFullYear());

const CONTACT_ADDRESS = {
  company: 'Dataland GmbH',
  street: 'Am Steinernen Stock 1',
  city: '60320 Frankfurt am Main',
  country: 'Germany',
} as const;

const CONTACT_PHONE = 'tel:+491622631304';
const CONTACT_PHONE_DISPLAY = '+49 162 263 1304';

const CONTACT_EMAIL = 'mailto:info@dataland.com';
const CONTACT_EMAIL_DISPLAY = 'info@dataland.com';

const CONTACT_INQUIRY_MAILTO =
    'mailto:info@dataland.com?subject=Inquiry%20about%20Dataland%20membership&body=Dear%20Dataland%20team%2C%0A%0AI%20am%20interested%20in%20learning%20more%20about%20Dataland%20membership.%0A%0AName%3A%20%0ACompany%3A%20%0ARole%3A%20%0A%0APlease%20get%20back%20to%20me%20at%20your%20earliest%20convenience.%0A%0ABest%20regards';

interface NavigationSection {
  href: string;
  label: string;
}

const aboutSections: NavigationSection[] = [
  { href: '/about#company', label: 'Company' },
  { href: '/about#team', label: 'Leadership team' },
  { href: '/about#partners', label: 'Integration partners' },
  { href: '/about#updates', label: 'News and updates' },
  { href: '/about#contact', label: 'Get in touch' },
];

const productSections: NavigationSection[] = [
  { href: '/product#how-it-works', label: 'How it works' },
  { href: '/product#getting-data', label: 'Getting the data you need' },
  { href: '/product#features', label: 'Platform features' },
  { href: '/product#use-cases', label: 'Use cases' },
  { href: '/product#customer-stories', label: 'Customer stories' },
  { href: '/product#membership-pricing', label: 'Membership and pricing' },
  { href: '/product#documentation', label: 'Documentation' },
];
</script>

<style scoped>
.footer {
  background: #f7f7f5;
  color: #1b1b1b;
  font-family: 'IBM Plex Sans', sans-serif;
  text-align: left;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.footer svg {
  display: block;
}

.footer__top {
  border-top: 1px solid #e6e6e6;
  display: grid;
  grid-template-columns: 1fr;
  gap: 2.5rem;
  max-width: 1280px;
  margin: 0 auto;
  padding: 2.5rem 1.5rem;
}

.footer__col {
  display: flex;
  flex-direction: column;
}

.footer__col--contact {
  gap: 1rem;
}

.footer__logo-link {
  display: inline-flex;
  width: 100%;
  max-width: 192px;
}

.footer__logo-img {
  width: 100%;
  height: auto;
  object-fit: contain;
}

.footer__contact-block {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.footer__contact-address {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  color: #737373;
}

.footer__company-name {
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0;
  color: #1b1b1b;
}

.footer__address-row {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.footer__icon--location {
  margin-top: 0.375rem;
  flex-shrink: 0;
  align-self: flex-start;
}

.footer__address-lines {
  margin-top: 0.125rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.footer__address-line {
  margin: 0;
  font-size: 0.82rem;
  color: #737373;
}

.footer__contact-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.82rem;
  color: #737373;
  text-decoration: none;
  transition: color 150ms ease-in-out;
}

.footer__contact-link:hover {
  color: #e67e22;
}

.footer__icon {
  color: currentColor;
}

.footer__col-heading {
  font-size: 0.95rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  line-height: 1.5;
  margin: 0 0 1rem;
  color: #1b1b1b;
}

.footer__link-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.625rem;
}

.footer__list-link {
  font-size: 1rem;
  color: #737373;
  text-decoration: none;
  text-align: left;
  transition: color 150ms ease-in-out;
}

.footer__list-link:hover {
  color: #e67e22;
}

.footer__list-link--inline {
  display: inline-flex;
  align-items: center;
  gap: 0.625rem;
}

.footer__bottom {
  border-top: 1px solid #e6e6e6;
}

.footer__bottom-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

.footer__bottom-left {
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
  align-items: center;
}

.footer__bottom-link {
  display: inline-flex;
  align-items: center;
  min-height: 44px;
  min-width: 44px;
  font-size: 0.8125rem;
  color: #737373;
  text-decoration: none;
  transition: color 150ms ease-in-out;
}

.footer__bottom-link:hover {
  color: #ff6813;
}

.footer__bottom-right {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.8125rem;
  color: #737373;
}

.footer__cookie-btn {
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
  font-family: inherit;
}

.footer__copyright {
  font-size: inherit;
  color: inherit;
}

@media only screen and (min-width: 768px) {
  .footer__top {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media only screen and (min-width: 1024px) {
  .footer__top {
    padding-top: 3rem;
    padding-bottom: 3rem;
  }

  .footer__bottom-inner {
    flex-direction: row;
    justify-content: space-between;
  }

  .footer__bottom-left {
    padding-left: 4rem;
  }
}

@media only screen and (min-width: 1280px) {
  .footer__top {
    grid-template-columns: 1.4fr 1fr 1fr 1fr;
  }
}
</style>
