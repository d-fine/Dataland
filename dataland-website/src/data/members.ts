import { existsSync } from 'node:fs';
import path from 'node:path';

export interface TrustedByLogo {
  name: string;
  imagePath: string;
  className?: string;
  carouselContainerClassName?: string;
  gridContainerClassName?: string;
  imageFrameClassName?: string;
}

type TrustedByLogoMetadata = Omit<TrustedByLogo, 'imagePath'> & { imagePath?: string };

const PUBLIC_LOGOS_DIRECTORY = path.join(process.cwd(), 'public', 'static', 'logos');

const TRUSTED_BY_LOGO_FILENAMES = [
  'logo_atlas_metrics.svg',
  'logo_bantleon.svg',
  'logo_bayerninvest.svg',
  'logo_bayernlb_gross.svg',
  'logo_bvi.png',
  'logo_ChomCapital.png',
  'logo_deka.png',
  'logo_deutsche_rueck.svg',
  'logo_d-fine.svg',
  'logo_DYDONAI_x.png',
  'logo_Envoria.png',
  'logo_eurodat.svg',
  'logo_fmf.png',
  'logo_gleif_new.svg',
  'logo_hansa_invest.svg',
  'logo_impact_cubed.svg',
  'logo_KYT.svg',
  'logo_laiqon.svg',
  'logo_leonardo.jpg',
  'logo_meag_big.svg',
  'logo_nordlb.svg',
  'logo_ovb.png',
  'logo_pwc.svg',
  'logo_sustaind.svg',
  'logo_systems.svg',
  'logo_vgh.svg',
  'logo_vkb.svg',
  'logo_wertestiftung.png',
  'logo_iss-soprasteria.png',
  'logo_fact_Salbei.svg',
  'logo-eskua-ai.png',
  'logo-keynum.webp',
] as const;

const LOGO_FILE_ALIASES: Record<string, string> = {
  'logo_deka.png': 'logo_deka_x.png',
  'logo_systems.svg': 'logo_tsystems.svg',
};

const LOGO_METADATA_BY_FILENAME: Record<string, TrustedByLogoMetadata> = {
  'logo_atlas_metrics.svg': { name: 'Atlas Metrics' },
  'logo_bantleon.svg': { name: 'Bantleon' },
  'logo_bayerninvest.svg': { name: 'BayernInvest' },
  'logo_bayernlb_gross.svg': { name: 'BayernLB', className: 'scale-[1.0]' },
  'logo_bvi.png': { name: 'BVI' },
  'logo_ChomCapital.png': { name: 'Chom Capital', className: 'scale-[1.24]' },
  'logo_deka_x.png': { name: 'Deka', className: 'scale-[1.15]' },
  'logo_deutsche_rueck.svg': { name: 'Deutsche Rueck' },
  'logo_d-fine.svg': { name: 'd-fine' },
  'logo_DYDONAI_x.png': { name: 'DYDONAI', className: 'scale-[1.26]' },
  'logo_Envoria.png': { name: 'Envoria' },
  'logo_eurodat.svg': { name: 'EuroDat' },
  'logo_fmf.png': { name: 'FMF', className: 'scale-[1.12]' },
  'logo_gleif_new.svg': { name: 'GLEIF' },
  'logo_hansa_invest.svg': { name: 'Hansa-Invest' },
  'logo_impact_cubed.svg': { name: 'Impact Cubed' },
  'logo_KYT.svg': { name: 'KYT' },
  'logo_laiqon.svg': { name: 'Laiqon' },
  'logo_leonardo.jpg': { name: 'Leonardo', className: 'scale-[1.22]' },
  'logo_meag_big.svg': { name: 'MEAG' },
  'logo_nordlb.svg': { name: 'NORD/LB' },
  'logo_ovb.png': { name: 'OVB', className: 'scale-[1.08]' },
  'logo_pwc.svg': { name: 'PwC', className: 'scale-[1.28]' },
  'logo_sustaind.svg': { name: 'Sustaind', className: 'scale-[2.05]' },
  'logo_tsystems.svg': { name: 'T-Systems' },
  'logo_vgh.svg': { name: 'VGH', className: 'scale-[1.08]' },
  'logo_vkb.svg': { name: 'VKB', className: 'scale-[1.08]' },
  'logo_wertestiftung.png': { name: 'Werte-Stiftung' },
  'logo_iss-soprasteria.png': {
    name: 'ISS (Sopra Steria)',
  },
  'logo_fact_Salbei.svg': {
    name: 'FACT First Cloud',
    className: 'scale-[1.18]',
  },
  'logo-eskua-ai.png': {
    name: 'Eskua AI',
    imagePath: '/static/about/logo-eskua-ai.png',
    className: 'scale-[1.28]',
  },
  'logo-keynum.webp': {
    name: 'Keynum',
    imagePath: '/static/about/logo-keynum.webp',
    imageFrameClassName: 'rounded-[0.35rem] bg-[#111111] px-3 py-[0.2rem]',
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
  const canonicalFilename = LOGO_FILE_ALIASES[filename] ?? filename;
  const metadata = LOGO_METADATA_BY_FILENAME[canonicalFilename] ?? LOGO_METADATA_BY_FILENAME[filename];

  if (metadata?.imagePath) {
    return metadata.imagePath;
  }

  const publicLogoPath = path.join(PUBLIC_LOGOS_DIRECTORY, canonicalFilename);
  if (existsSync(publicLogoPath)) {
    return `/static/logos/${canonicalFilename}`;
  }

  throw new Error(
    `Trusted logo "${filename}" could not be resolved. ` +
      `Expected a file in public/static/logos or an explicit imagePath override.`,
  );
}

function createTrustedByLogo(filename: string): TrustedByLogo {
  const canonicalFilename = LOGO_FILE_ALIASES[filename] ?? filename;
  const metadata = LOGO_METADATA_BY_FILENAME[canonicalFilename] ?? LOGO_METADATA_BY_FILENAME[filename];

  return {
    name: metadata?.name ?? humanizeFilename(canonicalFilename),
    imagePath: resolveLogoImagePath(filename),
    className: metadata?.className,
    carouselContainerClassName: metadata?.carouselContainerClassName,
    gridContainerClassName: metadata?.gridContainerClassName,
    imageFrameClassName: metadata?.imageFrameClassName,
  };
}

export const TRUSTED_BY_LOGOS: TrustedByLogo[] = TRUSTED_BY_LOGO_FILENAMES.map(createTrustedByLogo);
