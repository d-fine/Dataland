export interface TrustPillar {
  icon: string;
  title: string;
  description: string;
}

export interface Person {
  name: string;
  role: string;
  bio: string;
  imagePath: string;
}

export interface AdvisoryPerson {
  name: string;
  role: string;
  organisation: string;
  imagePath: string;
  url?: string;
}

export interface Logo {
  name: string;
  imagePath: string;
}

export interface Principle {
  icon: string;
  title: string;
  description: string;
}

export const TRUST_PILLARS: TrustPillar[] = [
  {
    icon: 'pi pi-lock',
    title: 'Cannot be sold',
    description:
      '100% owned by Werte-Stiftung, a Frankfurt charitable foundation. Non-commercial by structure, not just by policy.',
  },
  {
    icon: 'pi pi-shield',
    title: 'Institutionally backed',
    description:
      'Backed by d-fine, PwC, and the leadership of BVI and VOEB — established names in German financial services.',
  },
  {
    icon: 'pi pi-chart-bar',
    title: 'Narrow scope',
    description: 'Makes published sustainability data accessible. No ratings, no assessments, no commercial agenda.',
  },
  {
    icon: 'pi pi-microchip',
    title: 'Transparent Technology',
    description:
      'Human-supervised AI extraction applied to public company disclosures — always with expert review, always with full traceability.',
  },
];

export const LEADERSHIP_TEAM: Person[] = [
  {
    name: 'Moritz Kiese',
    role: 'Managing Director',
    bio: 'Moritz leads Dataland with a focus on open-source sustainability infrastructure and European ESG data standards.',
    imagePath: '/static/images/Moritz_Kiese.jpg',
  },
  {
    name: 'Andreas Höcherl',
    role: 'Head of Product',
    bio: 'Andreas shapes the product vision, working closely with regulatory stakeholders and institutional members.',
    imagePath: '/static/about/team-andreas-hoecherl.svg', // [PLACEHOLDER] SVG avatar with initials. Replace with real photo.
  },
  {
    name: 'Sören Vorsmann',
    role: 'Head of Operations',
    bio: 'Sören oversees platform operations, infrastructure, and member onboarding.',
    imagePath: '/static/about/team-soeren-vorsmann.svg', // [PLACEHOLDER] SVG avatar with initials. Replace with real photo.
  },
];

export const ADVISORY_BOARD: AdvisoryPerson[] = [
  {
    name: 'Rudi Siebel',
    role: 'Advisory Board Member',
    organisation: 'BVI',
    imagePath: '/static/about/team-rudi-siebel.svg', // [PLACEHOLDER] SVG avatar with initials. Replace with real photo.
    url: 'https://www.bvi.de',
  },
  {
    name: 'Stephan Henkel',
    role: 'Advisory Board Member',
    organisation: 'VOEB',
    imagePath: '/static/about/team-stephan-henkel.svg', // [PLACEHOLDER] SVG avatar with initials. Replace with real photo.
    url: 'https://www.voeb.de',
  },
];

export const SPONSORS: Logo[] = [
  { name: 'T-Systems', imagePath: '/static/logos/img_t_systems.png' },
  { name: 'd-fine', imagePath: '/static/logos/img_d-fine.png' },
  { name: 'PwC', imagePath: '/static/logos/img_pwc.png' },
  { name: 'Experience One', imagePath: '/static/logos/img_Experience_One.png' },
];

export const PARTNERS: Logo[] = [
  { name: 'Eskua AI', imagePath: '/static/about/logo-eskua-ai.svg' }, // [PLACEHOLDER] SVG text logo. Replace with real logo.
  { name: 'Keynum', imagePath: '/static/about/logo-keynum.svg' }, // [PLACEHOLDER] SVG text logo. Replace with real logo.
  { name: 'FACT First Cloud', imagePath: '/static/about/logo-fact-first-cloud.svg' }, // [PLACEHOLDER] SVG text logo. Replace with real logo.
  { name: 'Sopra Steria', imagePath: '/static/about/logo-sopra-steria.svg' }, // [PLACEHOLDER] SVG text logo. Replace with real logo.
];

export const PRINCIPLES: Principle[] = [
  {
    icon: 'pi pi-verified',
    title: 'Integrity',
    description: 'We need comparable and reliable sustainability data to create value.',
  },
  {
    icon: 'pi pi-eye',
    title: 'Disclosure',
    description: 'We seek disclosure of sustainability data from our business relations.',
  },
  {
    icon: 'pi pi-unlock',
    title: 'Transparency',
    description: 'We respect and promote data sovereignty.',
  },
  {
    icon: 'pi pi-check-circle',
    title: 'Accountability',
    description: 'Data should be timely and easily accessible at fair cost.',
  },
  {
    icon: 'pi pi-balance-scale',
    title: 'Neutrality',
    description:
      'Common data spaces should be neutral, transparent, non-competitive and not-for-profit.',
  },
  {
    icon: 'pi pi-users',
    title: 'Collaboration',
    description: 'We work together to achieve these principles and promote their acceptance.',
  },
];

export const HERO_COPY = {
  headline: 'Who Stands Behind Dataland',
  subheadline:
    'A non-profit ESG data platform owned by a charitable foundation, backed by institutional leaders in German financial services.',
  ctaLabel: 'Get in Touch',
};

export const BOTTOM_CTA_COPY = {
  headline: 'Let Us Start the Conversation',
  subheadline:
    'Whether you want to consume data, contribute data, or support the platform as a sponsor \u2014 there is a place for you.',
  primaryCtaLabel: 'Talk to Our Team',
  secondaryCtaLabel: 'Learn More About Our Data',
};
