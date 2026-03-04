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
}

export interface Logo {
  name: string;
  imagePath: string;
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
    title: 'AI at non-profit scale',
    description: 'AI extraction applied exclusively to public company disclosures — efficiency without the commercial motive.',
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
    imagePath: '/static/about/team-andreas-hoecherl.webp',
  },
  {
    name: 'Sören Vorsmann',
    role: 'Head of Operations',
    bio: 'Sören oversees platform operations, infrastructure, and member onboarding.',
    imagePath: '/static/about/team-soeren-vorsmann.webp',
  },
];

export const ADVISORY_BOARD: AdvisoryPerson[] = [
  {
    name: 'Rudi Siebel',
    role: 'Advisory Board Member',
    organisation: 'BVI',
    imagePath: '/static/about/team-rudi-siebel.webp',
  },
  {
    name: 'Stephan Henkel',
    role: 'Advisory Board Member',
    organisation: 'VOEB',
    imagePath: '/static/about/team-stephan-henkel.webp',
  },
];

export const SPONSORS: Logo[] = [
  { name: 'T-Systems', imagePath: '/static/logos/img_t_systems.png' },
  { name: 'd-fine', imagePath: '/static/logos/img_d-fine.png' },
  { name: 'PwC', imagePath: '/static/logos/img_pwc.png' },
  { name: 'Experience One', imagePath: '/static/logos/img_Experience_One.png' },
];

export const PARTNERS: Logo[] = [
  { name: 'Eskua AI', imagePath: '/static/about/logo-eskua-ai.webp' },
  { name: 'Keynum', imagePath: '/static/about/logo-keynum.webp' },
  { name: 'FACT First Cloud', imagePath: '/static/about/logo-fact-first-cloud.webp' },
  { name: 'Sopra Steria', imagePath: '/static/about/logo-sopra-steria.webp' },
];

export const HERO_COPY = {
  headline: 'Who Stands Behind Dataland',
  subheadline:
    'A non-profit ESG data platform owned by a charitable foundation, backed by institutional leaders in German financial services.',
  ctaLabel: 'Get in Touch',
};

export const BOTTOM_CTA_COPY = {
  headline: 'Ready to Learn More?',
  subheadline: 'Have a question about Dataland? We would be glad to hear from you.',
  ctaLabel: 'Get in Touch',
};