/**
 * Utility function to convert specification service URLs to documentation routes
 */
export function convertSpecificationUrlToDocumentationRoute(url: string): string {
  if (!url) return url;

  // Parse the URL to extract the path and components
  try {
    const urlObj = new URL(url);
    const path = urlObj.pathname;

    // Match patterns like /specifications/frameworks/{id}
    const frameworkMatch = path.match(/\/specifications\/frameworks\/(.+)/);
    if (frameworkMatch) {
      const frameworkId = frameworkMatch[1];
      return `/documentation/frameworks/${frameworkId}`;
    }

    // Match patterns like /specifications/data-point-types/{id}
    const dataPointMatch = path.match(/\/specifications\/data-point-types\/(.+)/);
    if (dataPointMatch) {
      const dataPointId = dataPointMatch[1];
      return `/documentation/data-point-types/${dataPointId}`;
    }

    // Match patterns like /specifications/data-point-base-types/{id}
    const baseTypeMatch = path.match(/\/specifications\/data-point-base-types\/(.+)/);
    if (baseTypeMatch) {
      const baseTypeId = baseTypeMatch[1];
      return `/documentation/data-point-base-types/${baseTypeId}`;
    }

    // If no pattern matches, return the original URL
    return url;
  } catch (error) {
    // If URL parsing fails, return the original URL
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