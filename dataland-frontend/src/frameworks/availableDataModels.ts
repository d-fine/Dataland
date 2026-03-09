import type { Category } from '@/utils/GenericFrameworkTypes';

type DataModel = Category[];

type AvailableModel = {
  id: string;
  label: string;
  // optional eager data model
  dataModel?: DataModel;
  // lazy loader that returns the module exports from UploadConfig
  loader?: () => Promise<Record<string, any>>;
};

// Registry of data models available in the frontend. Use lazy loaders
// to avoid bundling large models into the main chunk. Loaders should
// return the module namespace from the corresponding UploadConfig.ts.
export const AVAILABLE_DATA_MODELS: AvailableModel[] = [
  {
    id: 'eutaxonomy-financials',
    label: 'EU Taxonomy (Financials)',
    loader: () => import('./eutaxonomy-financials/UploadConfig'),
  },
  {
    id: 'sfdr',
    label: 'SFDR',
    loader: () => import('./sfdr/UploadConfig'),
  },
  {
    id: 'pcaf',
    label: 'PCAF',
    loader: () => import('./pcaf/UploadConfig'),
  },
  {
    id: 'vsme',
    label: 'VSME',
    loader: () => import('./vsme/UploadConfig'),
  },
  {
    id: 'lksg',
    label: 'LKSg',
    loader: () => import('./lksg/UploadConfig'),
  },
  {
    id: 'nuclear-and-gas',
    label: 'Nuclear and Gas',
    loader: () => import('./nuclear-and-gas/UploadConfig'),
  },
];

// Helper to extract the actual data-model array from a loaded module.
export async function extractDataModel(module: Record<string, any>): Promise<DataModel | null> {
  if (!module) return null;
  // try default
  if (Array.isArray(module.default)) return module.default as DataModel;
  // try any named export that looks like *DataModel
  for (const k of Object.keys(module)) {
    if (k.toLowerCase().includes('datamodel') && Array.isArray(module[k])) return module[k] as DataModel;
  }
  // fallback: return the first exported array
  for (const k of Object.keys(module)) {
    if (Array.isArray(module[k])) return module[k] as DataModel;
  }
  return null;
}

export type { DataModel };
