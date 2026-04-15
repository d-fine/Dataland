export const INDEX_PAGE_CONTENT = {
  meta: {
    title: 'Dataland - Non-profit sustainability data',
  },
  hero: {
    ariaLabel: 'The open-source platform for structured sustainability data',
    titleLeading: 'Open-source platform for',
    titleAccent: 'structured sustainability data',
    strapline: 'Non-profit. Source-based. Unrestricted use.',
    stats: [
      { value: '1,000', label: 'Users' },
      { value: '3,000', label: 'Datasets' },
      { value: '300,000', label: 'Live data points' },
    ],
    buttons: {
      try: 'Try it free',
      more: 'More about us',
      contact: 'Get in touch',
    },
  },
  search: {
    title: 'Search sustainability data by company name or LEI',
  },
  whyUs: {
    title: 'Why financial institutions choose Dataland',
    intro: 'It comes down to how we solve common challenges in accessing, using, and sourcing ESG data.',
    buttons: {
      features: 'Discover platform features',
      useCases: 'Explore use cases',
      contact: 'Get in touch',
    },
  },
  trustedBy: {
    title: 'Trusted by',
    intro: 'A growing network of members, partners, and sponsors across Europe.',
  },
  customerStories: {
    title: 'How our members use Dataland',
    intro: 'A number of ESG data challenges are addressed across use cases in reporting, risk, and compliance.',
    readMore: 'Read more',
  },
  testimonials: {
    title: 'What our members say',
    intro: 'Insights from members, partners, and supporters based on their experience.',
    watch: 'Watch member testimonials',
  },
  frameworks: {
    title: 'Frameworks on Dataland',
    intro: 'The platform covers the ESG frameworks most relevant to European financial institutions.',
    cta: 'Explore how companies and investors report under these frameworks',
  },
  cta: {
    title: 'Ready to get started?',
    buttons: {
      contact: 'Get in touch',
      try: 'Try it free',
    },
  },
} as const;

export const COMMUNITY_PAGE_CONTENT = {
  meta: {
    title: 'Community - Dataland',
    description: 'See how Dataland supports ESG data needs across sectors, use cases, and reporting requirements.',
  },
  hero: {
    title:
      'Dataland combines flexibility and breadth to support ESG data needs for different institution types, operational models, and reporting requirements.',
    buttons: {
      stories: 'Read Customer Stories',
      useCases: 'Explore Use Cases',
      contact: 'Get in touch',
    },
  },
  trustedOrganizations: {
    title: 'Trusted organizations.',
    intro: 'A community of institutions from different industries, sizes, and operating models.',
  },
  partners: {
    title: 'Our partners strengthen the Dataland network.',
  },
  cta: {
    title: 'Interested in joining us?',
    intro: 'Talk to us about membership, partnerships, and how Dataland can support your organization.',
    contact: 'Get in touch',
  },
} as const;

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

export const PRODUCT_PAGE_CONTENT = {
  meta: {
    title: 'Product - Dataland',
    description: "Explore Dataland's ESG data platform features, frameworks, use cases, and pricing.",
  },
  hero: {
    titleLeading: 'Structured, source-based sustainability data',
    titleAccent: 'for regulatory reporting and investment analysis.',
    buttons: {
      try: 'Try it free',
      demo: 'Book a demo',
      contact: 'Get in touch',
    },
  },
  howItWorks: {
    title: 'How it works',
    intro: 'Access and use Dataland in the way that fits your team.',
  },
  gettingData: {
    title: 'Getting the data you need',
    intro: 'Members can use all data available on the platform or request additional datasets as needed.',
  },
  features: {
    title: 'Platform features',
    intro: 'Core platform capabilities for accessing, managing, and exporting Dataland data in daily workflows.',
  },
  useCases: {
    title: 'Use cases',
    intro: 'Dataland supports different ESG data use cases across reporting, analysis, and portfolio workflows.',
    supportText: 'These use cases support different business areas across the company.',
  },
  customerStories: {
    title: 'Customer stories',
    intro: 'Real-world experiences illustrate how ESG data challenges are addressed in practice.',
    labels: {
      challenge: 'Challenge',
      solution: 'Solution',
      value: 'Value',
    },
    readFullStory: 'Read full story',
  },
  pricing: {
    title: 'Membership and pricing',
    intro: 'A straightforward membership model with transparent pricing and shared sourcing costs.',
    annualMembership: {
      title: 'Annual membership',
      price: '5,000 EUR',
      intro: 'For this fee, everything already available on the platform is free for all Members:',
    },
    sharedCost: {
      title: 'Shared cost approach',
      intro: 'The cost of sourcing datasets is shared among members requesting the same data.',
      footnote: 'Dataset = 1 company, 1 framework, 1 reporting period.',
      highlight: 'The more members request the same company, the lower the cost per dataset.',
      membersHeader: '# of Members',
      costHeader: 'Cost (Credits)',
    },
    ctaTitle:
      'Join now to access high-quality ESG data and be part of building a non-profit European data infrastructure.',
    buttons: {
      contact: 'Get in touch',
      try: 'Try it free',
    },
  },
  documentation: {
    title: 'Documentation',
    intro: 'Technical documentation enables seamless integration and efficient use of the platform.',
    frameworkGuideTitle: 'Framework Guide',
    frameworkGuideLink: 'View on GitHub',
    apiReferenceTitle: 'API Reference',
    apiReferenceDescription: 'Interactive Swagger documentation for every Dataland service.',
    ctaTitle: 'Ready to get started?',
    buttons: {
      contact: 'Get in touch',
      try: 'Try it free',
    },
  },
} as const;

export const PARTNER_STORIES_PAGE_CONTENT = {
  meta: {
    title: 'Partner Stories - Dataland',
    description:
      'See how Dataland integration partners embed structured sustainability data into established platforms and workflows.',
  },
  hero: {
    eyebrow: 'Use cases',
    title: 'Integration partners',
    intro:
      'Dataland data is made available within established partner platforms, so Members can work through familiar interfaces, research flows, and reporting processes.',
  },
  sections: {
    detailsAriaLabel: 'Integration partner details',
    mattersAriaLabel: 'Why partner integration matters',
  },
  cta: {
    contact: 'Get in touch',
  },
} as const;
