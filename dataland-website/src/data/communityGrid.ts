export interface CommunityGridItem {
  title: string;
  text: string;
  icon: 'sectors' | 'sizes' | 'use_cases' | 'frameworks' | 'delivery' | 'collaboration';
}

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
