import { PARTNERS } from './about';

export interface TrustedByLogo {
  name: string;
  imagePath: string;
  className?: string;
  carouselContainerClassName?: string;
  gridContainerClassName?: string;
  imageFrameClassName?: string;
}

const MEMBER_LOGOS: TrustedByLogo[] = [
  { name: 'Atlas Metrics', imagePath: '/static/logos/logo_atlas_metrics.svg' },
  { name: 'Bantleon', imagePath: '/static/logos/logo_bantleon.svg' },
  { name: 'BayernInvest', imagePath: '/static/logos/logo_bayerninvest.svg' },
  { name: 'BayernLB', imagePath: '/static/logos/logo_bayernlb_gross.svg', className: 'scale-[1.0]' },
  { name: 'BVI', imagePath: '/static/logos/logo_bvi.png' },
  { name: 'Chom Capital', imagePath: '/static/logos/logo_ChomCapital.png', className: 'scale-[1.24]' },
  { name: 'd-fine', imagePath: '/static/logos/logo_d-fine.svg' },
  { name: 'EuroDat', imagePath: '/static/logos/logo_eurodat.svg' },
  { name: 'DYDONAI', imagePath: '/static/logos/logo_DYDONAI.png', className: 'scale-[1.26]' },
  { name: 'Envoria', imagePath: '/static/logos/logo_Envoria.png' },
  { name: 'Impact Cubed', imagePath: '/static/logos/logo_impact_cubed.svg' },
  { name: 'KYT', imagePath: '/static/logos/logo_KYT.svg' },
  { name: 'Laiqon', imagePath: '/static/logos/logo_laiqon.svg' },
  { name: 'Leonardo', imagePath: '/static/logos/logo_leonardo.jpg', className: 'scale-[1.22]' },
  { name: 'MEAG', imagePath: '/static/logos/logo_meag_2026.svg' },
  { name: 'Oeffentliche Versicherung Braunschweig', imagePath: '/static/logos/logo_Oeffentliche_Wort-Bildmarke_Blau_RGB.jpg', className: 'scale-[1.3]' },
  { name: 'Deutsche R\u00fcck', imagePath: '/static/logos/logo_deutsche_rueck.svg' },
  { name: 'Hansa-Invest', imagePath: '/static/logos/logo_hansa_invest.svg' },
  { name: 'NORD/LB', imagePath: '/static/logos/logo_nordlb.svg' },
  { name: 'PwC', imagePath: '/static/logos/logo_pwc.svg', className: 'scale-[1.28]' },
  { name: 'Sustaind', imagePath: '/static/logos/logo_sustaind.svg', className: 'scale-[2.05]' },
  { name: 'T-Systems', imagePath: '/static/logos/logo_tsystems.svg' },
  { name: 'Werte-Stiftung', imagePath: '/static/logos/logo_wertestiftung.png' },
];

const PARTNER_LOGOS: TrustedByLogo[] = PARTNERS.map((partner) => ({
  name: partner.name,
  imagePath: partner.imagePath,
  className:
    partner.name === 'Eskua AI'
      ? 'scale-[1.28]'
      : partner.name === 'FACT First Cloud'
        ? 'scale-[1.18]'
        : undefined,
  imageFrameClassName: partner.name === 'Keynum' ? 'rounded-[0.35rem] bg-[#111111] px-3 py-[0.2rem]' : undefined,
}));

export const TRUSTED_BY_LOGOS: TrustedByLogo[] = [...MEMBER_LOGOS, ...PARTNER_LOGOS];
