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
    name: 'Andreas H\u00F6cherl',
    role: 'Product Owner',
    bio: 'Andreas shapes the product vision, working closely with regulatory stakeholders and institutional members.',
    imagePath: '/static/about/team-andreas-hoecherl.jpg',
    email: 'mailto:andreas.hoecherl@dataland.com',
    linkedIn: 'https://www.linkedin.com/in/andreas-h%C3%B6cherl-016220b4/',
  },
  {
    name: 'S\u00F6ren Vorsmann',
    role: 'Operations & Customer Relations',
    bio: 'S\u00F6ren oversees platform operations, infrastructure, and member onboarding, ensuring a smooth platform experience.',
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
