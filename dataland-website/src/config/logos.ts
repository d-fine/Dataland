import { existsSync } from 'node:fs';
import path from 'node:path';

export interface MemberLogo {
  name: string;
  imagePath: string;
  scale?: number;
  className?: string;
  carouselContainerClassName?: string;
  gridContainerClassName?: string;
  imageFrameClassName?: string;
  category: 'member' | 'partner';
  url?: string;
}

type LogoMetadata = Omit<MemberLogo, 'imagePath'> & { imagePath?: string };

const PUBLIC_LOGOS_DIRECTORY = path.join(process.cwd(), 'public', 'static', 'logos');

const LOGO_METADATA_BY_FILENAME: Record<string, LogoMetadata> = {
  'logo_atlas_metrics.svg': { name: 'Atlas Metrics', category: 'member' },
  'logo_bantleon.svg': { name: 'Bantleon', category: 'member' },
  'logo_bayerninvest.svg': {
    name: 'BayernInvest',
    scale: 1.12,
    className: 'translate-x-[0.08rem]',
    category: 'member',
  },
  'logo_bayernlb_gross.svg': { name: 'BayernLB', scale: 1, category: 'member' },
  'logo_bvi.png': { name: 'BVI', scale: 0.76, category: 'member' },
  'logo_ChomCapital.png': { name: 'Chom Capital', scale: 1.12, category: 'member' },
  'logo_deka_x.png': { name: 'Deka', scale: 1.08, category: 'member' },
  'logo_deutsche_rueck.svg': { name: 'Deutsche Rueck', category: 'member' },
  'logo_d-fine.svg': { name: 'd-fine', scale: 0.76, category: 'partner', url: 'https://www.d-fine.com/' },
  'logo_DYDONAI_x.png': { name: 'DYDONAI', scale: 1.12, category: 'partner', url: 'https://dydon.ai/' },
  'logo_Envoria.png': { name: 'Envoria', category: 'member' },
  'logo_eurodat.svg': { name: 'EuroDat', category: 'partner', url: ' https://www.eurodat.org/' },
  'logo_fmf.png': { name: 'FMF', scale: 1.34, category: 'member' },
  'logo_gleif_new.svg': { name: 'GLEIF', category: 'partner', url: 'https://www.gleif.org/' },
  'logo_hansa_invest.svg': { name: 'Hansa-Invest', category: 'member' },
  'logo_laiqon.svg': { name: 'Laiqon', category: 'member' },
  'logo_meag_big.svg': { name: 'MEAG', category: 'member' },
  'logo_nordlb.svg': { name: 'NORD/LB', category: 'member' },
  'logo_ovb.png': { name: 'OVB', scale: 1.04, category: 'member' },
  'logo_pwc.svg': { name: 'PwC', scale: 1.42, category: 'member' },
  'logo_sustaind.svg': { name: 'Sustaind', scale: 1.4, category: 'partner', url: 'https://www.sustaind.de/' },
  'logo_tsystems.svg': { name: 'T-Systems', category: 'partner', url: 'https://www.t-systems.com/' },
  'logo_vgh.svg': { name: 'VGH', scale: 1.04, category: 'member' },
  'logo_vkb.svg': { name: 'VKB', scale: 1.04, category: 'member' },
  'logo_wertestiftung.png': { name: 'Werte-Stiftung', category: 'partner', url: 'https://wertestiftung.org/' },
  'logo_iss-soprasteria.png': {
    name: 'ISS (Sopra Steria)',
    category: 'partner',
    scale: 0.92,
    url: 'https://iss.soprasteria.de/',
  },
  'logo_fact_Salbei.svg': {
    name: 'FACT First Cloud',
    scale: 1.62,
    category: 'partner',
    url: 'https://www.fact.de/unsere-loesungen/first-cloud/',
  },
  'logo_eskua_salbei.svg': { name: 'Eskua AI', scale: 0.94, category: 'partner', url: 'https://www.eskua.ai/' },
  'logo_keynum.webp': {
    name: 'Keynum',
    imageFrameClassName: 'rounded-[0.35rem] bg-[#111111] px-3 py-[0.2rem]',
    category: 'partner',
    url: 'https://www.keynum.com/',
  },
};

function humanizeFilename(filename: string): string {
  const withoutPrefix = filename.replace(/^logo[_-]?/i, '').replace(/\.[^.]+$/, '');
  return withoutPrefix
    .split(/[_-]+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

function resolveLogoImagePath(filename: string): string {
  const metadata = LOGO_METADATA_BY_FILENAME[filename];

  if (metadata?.imagePath) {
    return metadata.imagePath;
  }

  const publicLogoPath = path.join(PUBLIC_LOGOS_DIRECTORY, filename);
  if (existsSync(publicLogoPath)) {
    return `/static/logos/${filename}`;
  }

  throw new Error(
    `Trusted logo "${filename}" could not be resolved. ` +
      `Expected a file in public/static/logos or an explicit imagePath override.`
  );
}

function createMemberLogo(filename: string): MemberLogo {
  const metadata = LOGO_METADATA_BY_FILENAME[filename];

  return {
    name: metadata?.name ?? humanizeFilename(filename),
    imagePath: resolveLogoImagePath(filename),
    scale: metadata?.scale,
    className: metadata?.className,
    carouselContainerClassName: metadata?.carouselContainerClassName,
    gridContainerClassName: metadata?.gridContainerClassName,
    imageFrameClassName: metadata?.imageFrameClassName,
    category: metadata?.category ?? 'member',
    url: metadata?.url,
  };
}

export const MEMBER_LOGOS: MemberLogo[] = Object.keys(LOGO_METADATA_BY_FILENAME)
  .filter((filename) => LOGO_METADATA_BY_FILENAME[filename].category === 'member')
  .map(createMemberLogo);

export const PARTNER_LOGOS: MemberLogo[] = Object.keys(LOGO_METADATA_BY_FILENAME)
  .filter((filename) => LOGO_METADATA_BY_FILENAME[filename].category === 'partner')
  .map(createMemberLogo);

export const ALL_LOGOS: MemberLogo[] = Object.keys(LOGO_METADATA_BY_FILENAME).map(createMemberLogo);
