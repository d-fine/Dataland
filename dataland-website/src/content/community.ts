export interface CommunityGridItem {
  title: string;
  text: string;
  icon: 'sectors' | 'sizes' | 'use_cases' | 'frameworks' | 'delivery' | 'collaboration';
}

export const COMMUNITY_PAGE_CONTENT = {
  meta: {
    title: 'Community - Dataland',
    description: 'See how Dataland supports ESG data needs across sectors, use cases, and reporting requirements.',
  },
  hero: {
    title:
      'Dataland combines flexibility and breadth to support ESG data needs for different institution types, operational models, and reporting requirements.',
    buttons: { stories: 'Read Customer Stories', useCases: 'Explore Use Cases', contact: 'Get in touch' },
  },
  members: {
    title: 'Our Members',
    intro: 'A selection of our members representing the Dataland community which spans different industries, organizational sizes, and operating models.',
  },
  partners: { title: 'Our partners aid us in building and operating our platform and strengthen the Dataland ecosystem.' },
  cta: {
    title: 'Interested in joining us?',
    intro: 'Talk to us about membership, partnerships, and how Dataland can support your organization.',
    contact: 'Get in touch',
  },
} as const;

export const COMMUNITY_GRID_ITEMS: CommunityGridItem[] = [
  {
    icon: 'sectors',
    title: 'Across industries',
    text: 'ESG data for banks, asset managers, insurers, data providers, and institutions from other sectors.',
  },
  {
    icon: 'sizes',
    title: 'Across company sizes',
    text: 'For institutions ranging from focused specialists to large, multi-entity organizations.',
  },
  {
    icon: 'use_cases',
    title: 'Across use cases',
    text: 'Sourcing, audit trail support, validation, reporting, template conversion and more in one setup.',
  },
  {
    icon: 'frameworks',
    title: 'Across reporting needs',
    text: 'Single platform covering multiple ESG and regulatory frameworks to avoid duplicating workflows.',
  },
  {
    icon: 'delivery',
    title: 'Across delivery models',
    text: 'Platform access, direct downloads, and API-based integration for different operational needs.',
  },
  {
    icon: 'collaboration',
    title: 'Across teams',
    text: 'Enabling reporting, risk, sustainability, and data teams to work on the same source-based datasets.',
  },
];
