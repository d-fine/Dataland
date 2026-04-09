export interface NewsItem {
  image: string;
  title: string;
  date: string;
  link: string;
}

export const URL_NEWS_EU_TAXO = 'https://www.linkedin.com/feed/update/urn:li:activity:7435335342439735296';
export const URL_NEWS_BVI_FOK = 'https://www.linkedin.com/feed/update/urn:li:activity:7432564767090905089';
export const URL_NEWS_DMM_Q12026 = 'https://www.linkedin.com/feed/update/urn:li:activity:7430511455638118400';
export const URL_NEWS_2025 = 'https://www.linkedin.com/feed/update/urn:li:activity:7419695872156028928';
export const URL_NEWS_SFDR2 = 'https://www.linkedin.com/feed/update/urn:li:activity:7404533321671589890';
export const URL_NEWS_PCAF = 'https://www.linkedin.com/feed/update/urn:li:activity:7397576850782441472';
export const URL_NEWS_SUST2025 = 'https://www.linkedin.com/feed/update/urn:li:activity:7395135444641947648';
export const URL_NEWS_DMMQ42025 = 'https://www.linkedin.com/feed/update/urn:li:activity:7392146170766151680';
export const URL_NEWS_ERIK = 'https://www.linkedin.com/feed/update/urn:li:activity:7391407400764772352';

export const NEWS_ITEMS: NewsItem[] = [
  {
    image: '/static/images/news/news_eu_taxo_x.png',
    title: 'Smooth transition to the new EU Taxonomy template',
    date: 'March 5, 2026',
    link: URL_NEWS_EU_TAXO,
  },
  {
    image: '/static/images/news/news_bvi_fok.png',
    title: 'Networking at BVI FOK',
    date: 'February 25, 2026',
    link: URL_NEWS_BVI_FOK,
  },
  {
    image: '/static/images/news/news_dmm_q12026_x.png',
    title: "Dataland Members' Meeting Q1 2026",
    date: 'February 20, 2026',
    link: URL_NEWS_DMM_Q12026,
  },
  {
    image: '/static/images/news/news_2025_x.png',
    title: '2025 in numbers',
    date: 'January 21, 2026',
    link: URL_NEWS_2025,
  },
  {
    image: '/static/images/news/news_sfdr2_x.png',
    title: 'How SFDR 2.0 reinforces the need for shared ESG data infrastructure',
    date: 'December 10, 2025',
    link: URL_NEWS_SFDR2,
  },
  {
    image: '/static/images/news/news_pcaf_x.png',
    title: 'PCAF on Dataland',
    date: 'November 21, 2025',
    link: URL_NEWS_PCAF,
  },
  {
    image: '/static/images/news/news_sust2025.png',
    title: 'Dataland @ Sustainability Kongress 2025',
    date: 'November 14, 2025',
    link: URL_NEWS_SUST2025,
  },
  {
    image: '/static/images/news/news_dmm_q42025_x.png',
    title: "Dataland Members' Meeting Q4 2025",
    date: 'November 6, 2025',
    link: URL_NEWS_DMMQ42025,
  },
  {
    image: '/static/images/news/news_erik.png',
    title: 'Leadership transition: thank you, Erik Breen!',
    date: 'November 4, 2025',
    link: URL_NEWS_ERIK,
  },
];
