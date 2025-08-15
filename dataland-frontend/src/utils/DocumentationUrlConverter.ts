/**
 * Internal helper to convert specification paths to documentation paths
 */
function convertSpecificationPath(path: string): string | null {
  // Match patterns like /specifications/frameworks/{id}
  const frameworkMatch = path.match(/\/specifications\/frameworks\/(.+)/);
  if (frameworkMatch) {
    return `/documentation/frameworks/${frameworkMatch[1]}`;
  }

  // Match patterns like /specifications/data-point-types/{id}
  const dataPointMatch = path.match(/\/specifications\/data-point-types\/(.+)/);
  if (dataPointMatch) {
    return `/documentation/data-point-types/${dataPointMatch[1]}`;
  }

  // Match patterns like /specifications/data-point-base-types/{id}
  const baseTypeMatch = path.match(/\/specifications\/data-point-base-types\/(.+)/);
  if (baseTypeMatch) {
    return `/documentation/data-point-base-types/${baseTypeMatch[1]}`;
  }

  return null;
}

/**
 * Utility function to convert specification service URLs to documentation routes
 */
export function convertSpecificationUrlToDocumentationRoute(url: string): string {
  if (!url) return url;

  try {
    const urlObj = new URL(url);
    const documentationPath = convertSpecificationPath(urlObj.pathname);
    return documentationPath || url;
  } catch (error) {
    console.warn('Failed to parse specification URL:', url, error);
    return url;
  }
}

/**
 * Utility function to convert specification service URLs to full documentation URLs for display
 */
export function convertSpecificationUrlToDocumentationUrl(url: string): string {
  if (!url) return url;

  try {
    const urlObj = new URL(url);
    const documentationPath = convertSpecificationPath(urlObj.pathname);
    return documentationPath ? `${window.location.origin}${documentationPath}` : url;
  } catch (error) {
    console.warn('Failed to parse specification URL:', url, error);
    return url;
  }
}

/**
 * Check if a URL is a specification service URL that should be converted
 */
export function isSpecificationServiceUrl(url: string): boolean {
  if (!url) return false;

  try {
    const urlObj = new URL(url);
    return urlObj.pathname.startsWith('/specifications/');
  } catch (error) {
    return false;
  }
}
