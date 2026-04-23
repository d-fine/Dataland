export interface NavigationSection {
  href: string;
  label: string;
}

export interface HeaderLink {
  href: string;
  label: string;
}

export interface HeaderAction extends HeaderLink {
  testId: string;
}

export const ABOUT_SECTIONS = [
  { href: '/about#company', label: 'Company' },
  { href: '/about#team', label: 'Leadership team' },
  { href: '/about#partners', label: 'Integration partners' },
  { href: '/about#updates', label: 'News and updates' },
  { href: '/about#contact', label: 'Get in touch' },
] as const;

export const PRODUCT_SECTIONS = [
  { href: '/product#how-it-works', label: 'How it works' },
  { href: '/product#getting-data', label: 'Getting the data you need' },
  { href: '/product#features', label: 'Platform features' },
  { href: '/product#use-cases', label: 'Use cases' },
  { href: '/product#customer-stories', label: 'Customer stories' },
  { href: '/product#membership-pricing', label: 'Membership and pricing' },
  { href: '/product#documentation', label: 'Documentation' },
] as const;

export const COMMUNITY_SECTIONS = [
  { href: '/network#our-members', label: 'Our Members' },
  { href: '/network#partners', label: 'Partners' },
] as const;

export const HEADER_NAV_GROUPS = [
  { href: '/about', label: 'About', sections: ABOUT_SECTIONS },
  { href: '/product', label: 'Product', sections: PRODUCT_SECTIONS },
  { href: '/network', label: 'Community', sections: COMMUNITY_SECTIONS },
] as const;

export const HEADER_MOBILE_LINKS = [
  { href: '/', label: 'Home' },
  ...HEADER_NAV_GROUPS.map(({ href, label }) => ({ href, label })),
] as const;

export const HEADER_ACTIONS = {
  login: { href: '/login', label: 'Login', testId: 'login-dataland-button' },
  signup: { href: '/register', label: 'Try it free', testId: 'signup-dataland-button' },
  platform: { href: '/platform-redirect', label: 'Back to platform', testId: 'backToPlatformLink' },
} as const satisfies Record<string, HeaderAction>;
