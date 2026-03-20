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
  email: string;
  linkedIn: string;
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

export interface Partner {
  name: string;
  imagePath: string;
  url: string;
}

export const LEADERSHIP_TEAM: Person[] = [
  {
    name: 'Moritz Kiese',
    role: 'Managing Director',
    bio: 'Moritz leads Dataland with a focus on open-source sustainability infrastructure and European ESG data standards.',
    imagePath: '/static/images/Moritz_Kiese.jpg',
    email: 'mailto:moritz.kiese@dataland.com',
    linkedIn: 'https://www.linkedin.com/in/moritz-kiese-932b104/',
  },
  {
    name: 'Andreas Pusch',
    role: 'Product Owner',
    bio: 'Andreas shapes the product vision, working closely with regulatory stakeholders and institutional members.',
    imagePath: '/static/about/team-andreas-hoecherl.svg',
    email: 'mailto:andreas.hoecherl@dataland.com',
    linkedIn: 'https://www.linkedin.com/in/andreas-h%C3%B6cherl-016220b4/',
  },
  {
    name: 'Soeren Vorsmann',
    role: 'Operations & Customer Relations',
    bio: 'Soeren oversees platform operations, infrastructure, and member onboarding.',
    imagePath: '/static/about/team-soeren-vorsmann.svg',
    email: 'mailto:soeren.vorsmann@dataland.com',
    linkedIn: 'https://www.linkedin.com/company/dataland-gmbh',
  },
];

export const PARTNERS: Partner[] = [
  {
    name: 'FACT First Cloud',
    imagePath: '/static/about/logo-fact-first-cloud.svg',
    url: 'https://www.fact.de/unsere-loesungen/first-cloud/',
  },
  {
    name: 'ISS (Sopra Steria)',
    imagePath: '/static/about/logo-sopra-steria.svg',
    url: 'https://iss.soprasteria.de/',
  },
];

export const COMPANY_COPY = {
  title: 'Company',
  text1:
    'Dataland is a non-profit initiative building shared infrastructure for high-quality sustainability data. The platform provides structured, source-based ESG data to financial institutions across Europe.',
  text2: 'Dataland is part of the Werte-Stiftung foundation, with strategic support from d-fine and PwC.',
};
