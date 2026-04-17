import { existsSync } from 'node:fs';
import path from 'node:path';

export interface TrustedByLogo {
  name: string;
  imagePath: string;
  scale?: number;
  className?: string;
  carouselContainerClassName?: string;
  gridContainerClassName?: string;
  imageFrameClassName?: string;
}

type TrustedByLogoMetadata = Omit<TrustedByLogo, 'imagePath'> & { imagePath?: string };

const PUBLIC_LOGOS_DIRECTORY = path.join(process.cwd(), 'public', 'static', 'logos');

const LOGO_METADATA_BY_FILENAME: Record<string, TrustedByLogoMetadata> = {
  'logo_atlas_metrics.svg': { name: 'Atlas Metrics' },
  'logo_bantleon.svg': { name: 'Bantleon' },
  'logo_bayerninvest.svg': { name: 'BayernInvest', scale: 1.12, className: 'translate-x-[0.08rem]' },
  'logo_bayernlb_gross.svg': { name: 'BayernLB', scale: 1.0 },
  'logo_bvi.png': { name: 'BVI' },
  'logo_ChomCapital.png': { name: 'Chom Capital', scale: 1.12 },
  'logo_deka_x.png': { name: 'Deka', scale: 1.08 },
  'logo_deutsche_rueck.svg': { name: 'Deutsche Rueck' },
  'logo_d-fine.svg': { name: 'd-fine' },
  'logo_DYDONAI_x.png': { name: 'DYDONAI', scale: 1.12 },
  'logo_Envoria.png': { name: 'Envoria' },
  'logo_eurodat.svg': { name: 'EuroDat' },
  'logo_fmf.png': { name: 'FMF', scale: 1.34 },
  'logo_gleif_new.svg': { name: 'GLEIF' },
  'logo_hansa_invest.svg': { name: 'Hansa-Invest' },
  'logo_impact_cubed.svg': { name: 'Impact Cubed' },
  'logo_KYT.svg': { name: 'KYT' },
  'logo_laiqon.svg': { name: 'Laiqon' },
  'logo_meag_big.svg': { name: 'MEAG' },
  'logo_nordlb.svg': { name: 'NORD/LB' },
  'logo_ovb.png': { name: 'OVB', scale: 1.04 },
  'logo_pwc.svg': { name: 'PwC', scale: 1.12 },
  'logo_sustaind.svg': { name: 'Sustaind', scale: 1.4 },
  'logo_tsystems.svg': { name: 'T-Systems' },
  'logo_vgh.svg': { name: 'VGH', scale: 1.04 },
  'logo_vkb.svg': { name: 'VKB', scale: 1.04 },
  'logo_wertestiftung.png': { name: 'Werte-Stiftung' },
  'logo_iss-soprasteria.png': { name: 'ISS (Sopra Steria)' },
  'logo_fact_Salbei.svg': { name: 'FACT First Cloud', scale: 1.36 },
  'logo_eskua_salbei.svg': { name: 'Eskua AI', scale: 0.88 },
  'logo_keynum.webp': {
    name: 'Keynum',
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

function createTrustedByLogo(filename: string): TrustedByLogo {
  const metadata = LOGO_METADATA_BY_FILENAME[filename];

  return {
    name: metadata?.name ?? humanizeFilename(filename),
    imagePath: resolveLogoImagePath(filename),
    scale: metadata?.scale,
    className: metadata?.className,
    carouselContainerClassName: metadata?.carouselContainerClassName,
    gridContainerClassName: metadata?.gridContainerClassName,
    imageFrameClassName: metadata?.imageFrameClassName,
  };
}

export const TRUSTED_BY_LOGOS: TrustedByLogo[] = Object.keys(LOGO_METADATA_BY_FILENAME).map(createTrustedByLogo);
