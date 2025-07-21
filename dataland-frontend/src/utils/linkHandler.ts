/**
 * Utility functions for handling specification links and converting them to internal documentation routes
 */

export const isDataPointTypeRef = (url: string): boolean => {
  return url.includes('/specifications/data-point-types/');
};

export const isDataPointBaseTypeRef = (url: string): boolean => {
  return url.includes('/specifications/data-point-base-types/');
};

export const isFrameworkRef = (url: string): boolean => {
  return url.includes('/specifications/frameworks/');
};

export const extractDataPointTypeId = (url: string): string => {
  const match = url.match(/\/specifications\/data-point-types\/(.+)$/);
  return match ? match[1] : '';
};

export const extractDataPointBaseTypeId = (url: string): string => {
  const match = url.match(/\/specifications\/data-point-base-types\/(.+)$/);
  return match ? match[1] : '';
};

export const extractFrameworkId = (url: string): string => {
  const match = url.match(/\/specifications\/frameworks\/(.+)$/);
  return match ? match[1] : '';
};

export const convertToDocumentationRoute = (specificationUrl: string): string => {
  if (isDataPointTypeRef(specificationUrl)) {
    const id = extractDataPointTypeId(specificationUrl);
    return `/documentation/data-point-types/${id}`;
  }
  
  if (isDataPointBaseTypeRef(specificationUrl)) {
    const id = extractDataPointBaseTypeId(specificationUrl);
    return `/documentation/data-point-base-types/${id}`;
  }
  
  if (isFrameworkRef(specificationUrl)) {
    const id = extractFrameworkId(specificationUrl);
    return `/documentation/frameworks/${id}`;
  }
  
  // Fallback for unknown URLs
  return specificationUrl;
};

export const handleSpecificationLink = (ref: string): void => {
  if (isDataPointTypeRef(ref) || isDataPointBaseTypeRef(ref) || isFrameworkRef(ref)) {
    // Convert to internal documentation route and open in new tab
    const route = convertToDocumentationRoute(ref);
    window.open(route, '_blank');
  } else {
    // Open external links in a new tab
    window.open(ref, '_blank');
  }
};