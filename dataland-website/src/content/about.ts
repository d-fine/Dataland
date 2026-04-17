export interface Person {
  name: string;
  role: string;
  bio: string;
  imagePath: string;
  email: string;
  linkedIn: string;
}

export interface Partner {
  name: string;
  imagePath: string;
  url: string;
  scale?: number;
  darkBg?: boolean;
}

export interface NewsItem {
  image: string;
  title: string;
  date: string;
  link: string;
}

export const ABOUT_PAGE_CONTENT = {
  meta: {
    title: 'About - Dataland',
    description: "Learn about Dataland's mission, team, and partners.",
  },
  company: {
    title: 'Built for Europe, in Europe',
    supportLine: {
      beforeFoundation: 'Dataland is part of the ',
      betweenFoundationAndDfine: ' foundation, with strategic support from ',
      betweenDfineAndPwc: ' and ',
      afterPwc: '.',
    },
  },
  leadership: {
    title: 'Leadership team',
    intro: 'The leadership team combines expertise in data, technology, and financial services.',
  },
  integrationPartners: {
    title: 'Integration partners',
    intro:
      'Dataland data is embedded into partner platforms, enabling Members to seamlessly access sustainability data through familiar interfaces and workflows.',
  },
  news: {
    title: 'News and updates',
    intro: "Recent updates, announcements, and insights reflect Dataland's ongoing development.",
  },
  contact: {
    eyebrow: 'Contact',
    title: 'Bring sustainability data into your workflow.',
    intro:
      'Whether you are exploring membership, a partnership, or a product deep dive, we can point you to the right next step.',
    officeLabel: 'Office',
    directLabel: 'Direct',
  },
  startPanel: {
    eyebrow: 'Start here',
    title: 'Pick the path that fits what you want to do next.',
    intro: 'Reach out directly, stay up to date, or start exploring the platform on your own.',
    buttons: {
      contact: 'Get in touch',
      documentation: 'View documentation',
      try: 'Try it free',
    },
  },
} as const;

export const LEADERSHIP_TEAM: Person[] = [
  {
    name: 'Moritz Kiese',
    role: 'Managing Director',
    bio: 'Moritz leads Dataland with a focus on open-source sustainability infrastructure and European ESG data standards.',
    imagePath: '/static/about/team_Moritz_Kiese.jpg',
    email: 'mailto:moritz.kiese@dataland.com',
    linkedIn: 'https://www.linkedin.com/in/moritz-kiese-932b104/',
  },
  {
    name: 'Andreas Hoecherl',
    role: 'Product Owner',
    bio: 'Andreas shapes the product vision, working closely with regulatory stakeholders and institutional members.',
    imagePath: '/static/about/team-andreas-hoecherl.jpg',
    email: 'mailto:andreas.hoecherl@dataland.com',
    linkedIn: 'https://www.linkedin.com/in/andreas-h%C3%B6cherl-016220b4/',
  },
  {
    name: 'Soeren Vorsmann',
    role: 'Operations & Customer Relations',
    bio: 'Soeren oversees platform operations, infrastructure, and member onboarding, ensuring a smooth platform experience.',
    imagePath: '/static/about/team-soeren-vorsmann.jpg',
    email: 'mailto:soeren.vorsmann@dataland.com',
    linkedIn: 'https://www.linkedin.com/company/dataland-gmbh',
  },
];

export const PARTNERS: Partner[] = [
  {
    name: 'FACT First Cloud',
    imagePath: '/static/logos/logo_fact_Salbei.svg',
    url: 'https://www.fact.de/unsere-loesungen/first-cloud/',
    scale: 1.62,
  },
  {
    name: 'ISS (Sopra Steria)',
    imagePath: '/static/logos/logo_iss-soprasteria.png',
    url: 'https://iss.soprasteria.de/',
    scale: 0.92,
  },
  {
    name: 'Eskua AI',
    imagePath: '/static/logos/logo_eskua_salbei.svg',
    url: 'https://www.eskua.ai/',
    scale: 0.94,
  },
  {
    name: 'Keynum',
    imagePath: '/static/logos/logo_keynum.webp',
    url: 'https://www.keynum.com/',
    darkBg: true,
  },
];

export const COMPANY_COPY = {
  title: 'Company',
  text1:
    'Dataland is a non-profit initiative building shared infrastructure for high-quality sustainability data. The platform provides structured, source-based ESG data to financial institutions across Europe.',
  text2: 'Dataland is part of the Werte-Stiftung foundation, with strategic support from d-fine and PwC.',
};

export const NEWS_ITEMS: NewsItem[] = [
  {
    image: '/static/images/news/news_eu_taxo_x.png',
    title: 'Smooth transition to the new EU Taxonomy template',
    date: 'March 5, 2026',
    link: 'https://www.linkedin.com/feed/update/urn:li:activity:7435335342439735296',
  },
  {
    image: '/static/images/news/news_bvi_fok.png',
    title: 'Networking at BVI FOK',
    date: 'February 25, 2026',
    link: 'https://www.linkedin.com/feed/update/urn:li:activity:7432564767090905089',
  },
  {
    image: '/static/images/news/news_dmm_q12026_x.png',
    title: "Dataland Members' Meeting Q1 2026",
    date: 'February 20, 2026',
    link: 'https://www.linkedin.com/feed/update/urn:li:activity:7430511455638118400',
  },
  {
    image: '/static/images/news/news_2025_x.png',
    title: '2025 in numbers',
    date: 'January 21, 2026',
    link: 'https://www.linkedin.com/feed/update/urn:li:activity:7419695872156028928',
  },
  {
    image: '/static/images/news/news_sfdr2_x.png',
    title: 'How SFDR 2.0 reinforces the need for shared ESG data infrastructure',
    date: 'December 10, 2025',
    link: 'https://www.linkedin.com/feed/update/urn:li:activity:7404533321671589890',
  },
  {
    image: '/static/images/news/news_pcaf_x.png',
    title: 'PCAF on Dataland',
    date: 'November 21, 2025',
    link: 'https://www.linkedin.com/feed/update/urn:li:activity:7397576850782441472',
  },
  {
    image: '/static/images/news/news_sust2025.png',
    title: 'Dataland @ Sustainability Kongress 2025',
    date: 'November 14, 2025',
    link: 'https://www.linkedin.com/feed/update/urn:li:activity:7395135444641947648',
  },
  {
    image: '/static/images/news/news_dmm_q42025_x.png',
    title: "Dataland Members' Meeting Q4 2025",
    date: 'November 6, 2025',
    link: 'https://www.linkedin.com/feed/update/urn:li:activity:7392146170766151680',
  },
  {
    image: '/static/images/news/news_erik.png',
    title: 'Leadership transition: thank you, Erik Breen!',
    date: 'November 4, 2025',
    link: 'https://www.linkedin.com/feed/update/urn:li:activity:7391407400764772352',
  },
];

export const CONTACT_PHONE = 'tel:+491622631304';
export const CONTACT_PHONE_DISPLAY = '+49 162 263 1304';
export const CONTACT_EMAIL = 'mailto:info@dataland.com';
export const CONTACT_EMAIL_DISPLAY = 'info@dataland.com';
export const CONTACT_INQUIRY_MAILTO =
  'mailto:info@dataland.com?subject=Inquiry%20about%20Dataland%20membership&body=Dear%20Dataland%20team%2C%0A%0AI%20am%20interested%20in%20learning%20more%20about%20Dataland%20membership.%0A%0AName%3A%20%0ACompany%3A%20%0ARole%3A%20%0A%0APlease%20get%20back%20to%20me%20at%20your%20earliest%20convenience.%0A%0ABest%20regards';
export const CONTACT_ADDRESS = {
  company: 'Dataland GmbH',
  street: 'Am Steinernen Stock 1',
  city: '60320 Frankfurt am Main',
  country: 'Germany',
};
