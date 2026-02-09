import { ref, shallowRef, type Ref } from 'vue';
import type { SimpleFrameworkSpecification, FrameworkSpecification, DataPointTypeSpecification } from '@clients/specificationservice';
import type {
  FrameworkSpecificationWithParsedSchema,
  ParsedSchemaNode,
  ParsedSection,
  ParsedDataPoint,
  DataPointTypeReference,
} from '@/types/Specifications';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';

/**
 * Options for the useSpecifications composable following Dataland pattern.
 * Fetch functions are injected for testability.
 */
export interface UseSpecificationsOptions {
  /**
   * Function to fetch the list of available frameworks.
   * Should return a promise resolving to an array of framework summaries.
   */
  fetchFrameworks: () => Promise<SimpleFrameworkSpecification[]>;

  /**
   * Function to fetch a specific framework specification by ID.
   * Should return a promise resolving to the complete framework specification.
   * @param id - The framework specification ID
   */
  fetchSpecification: (id: string) => Promise<FrameworkSpecification>;
  
  /**
   * Optional function to fetch data point type details.
   * Required for batch data point loading (Phase 2).
   * @param id - The data point type ID
   */
  fetchDataPointDetails?: (id: string) => Promise<DataPointTypeSpecification>;
  
  /**
   * Enable batch loading of data point details.
   * When true, loads business definitions and metadata for all data points.
   * Default: false
   */
  enableBatchDataPointLoading?: boolean;
}

/**
 * Composable for managing specification data and state.
 * Handles framework list fetching, framework selection, and schema parsing.
 * 
 * Following Dataland patterns:
 * - Accepts fetch functions as parameters for testability
 * - Uses shallowRef for large nested data structures
 * - Provides granular loading states for better UX
 * - Includes comprehensive error handling
 * 
 * @param options - Configuration options with fetch functions
 */
export function useSpecifications({ 
  fetchFrameworks, 
  fetchSpecification, 
  fetchDataPointDetails,
  enableBatchDataPointLoading = false,
}: UseSpecificationsOptions) {
  // Reactive state
  const frameworks = ref<SimpleFrameworkSpecification[]>([]);
  const selectedFramework = shallowRef<FrameworkSpecificationWithParsedSchema | null>(null);
  const isLoadingFrameworks = ref(false);
  const isLoadingSpecification = ref(false);
  const isLoadingDataPointDetails = ref(false);
  const error = ref<string | null>(null);
  
  // Session-level cache for data point details (shallowRef for performance)
  const dataPointCache = shallowRef<Map<string, DataPointTypeSpecification>>(new Map());

  /**
   * Load the list of available frameworks.
   * Sets loading state and handles errors.
   */
  async function loadFrameworks(): Promise<void> {
    try {
      isLoadingFrameworks.value = true;
      error.value = null;

      const frameworkList = await fetchFrameworks();
      
      // Sort frameworks alphabetically by name for better UX
      frameworks.value = frameworkList.sort((a, b) => a.name.localeCompare(b.name));
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load frameworks';
      error.value = message;
      console.error('Error loading frameworks:', err);
      throw err;
    } finally {
      isLoadingFrameworks.value = false;
    }
  }

  /**
   * Select and load a framework specification by ID.
   * Fetches the specification, parses the schema, and updates state.
   * Optionally loads batch data point details if enabled.
   * 
   * @param frameworkId - The ID of the framework to load
   */
  async function selectFramework(frameworkId: string): Promise<void> {
    try {
      isLoadingSpecification.value = true;
      error.value = null;

      const specification = await fetchSpecification(frameworkId);
      
      // Parse the schema JSON string into a hierarchical tree
      const parsedSchema = parseSchema(specification.schema);

      // Create the enhanced specification object
      selectedFramework.value = {
        ...specification,
        schemaJson: specification.schema,
        parsedSchema,
      };
      
      // Batch load data point details if enabled
      if (enableBatchDataPointLoading && fetchDataPointDetails) {
        await enrichSchemaWithDataPointDetails(parsedSchema);
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load specification';
      error.value = message;
      console.error(`Error loading specification for framework ${frameworkId}:`, err);
      
      // Clear selected framework on error
      selectedFramework.value = null;
      
      throw err;
    } finally {
      isLoadingSpecification.value = false;
    }
  }

  /**
   * Parse the schema JSON string into a hierarchical tree structure.
   * Recursively traverses the schema object to identify sections and data points.
   * 
   * Schema structure from API:
   * - Nested objects represent sections (containers)
   * - Objects with 'id' and 'ref' properties are data point references
   * 
   * @param schemaJson - The schema JSON string from the API
   * @returns Parsed schema tree as an array of root-level nodes
   */
  function parseSchema(schemaJson: string): ParsedSchemaNode[] {
    try {
      const schemaObject = JSON.parse(schemaJson);
      return parseSchemaObject(schemaObject, '');
    } catch (err) {
      console.error('Failed to parse schema JSON:', err);
      error.value = 'Failed to parse framework schema';
      return [];
    }
  }

  /**
   * Recursively parse a schema object into a tree of nodes.
   * 
   * @param obj - The schema object or sub-object to parse
   * @param parentPath - The path to this object (for generating unique IDs)
   * @returns Array of parsed schema nodes
   */
  function parseSchemaObject(obj: Record<string, unknown>, parentPath: string): ParsedSchemaNode[] {
    const nodes: ParsedSchemaNode[] = [];

    for (const [key, value] of Object.entries(obj)) {
      const currentPath = parentPath ? `${parentPath}.${key}` : key;

      if (isDataPointReference(value)) {
        // This is a data point (leaf node)
        const dataPoint: ParsedDataPoint = {
          type: 'dataPoint',
          id: currentPath,
          key,
          dataPointTypeId: value.id,
          ref: value.ref,
          aliasExport: value.aliasExport,
        };
        nodes.push(dataPoint);
      } else if (isObject(value)) {
        // This is a section (container with children)
        const children = parseSchemaObject(value as Record<string, unknown>, currentPath);
        
        const section: ParsedSection = {
          type: 'section',
          id: currentPath,
          key,
          label: humanizeKey(key),
          children,
        };
        nodes.push(section);
      }
      // Skip non-object values (shouldn't occur in valid schemas)
    }

    return nodes;
  }

  /**
   * Check if a value is a data point reference object.
   * Data points have 'id' and 'ref' properties.
   * 
   * @param value - The value to check
   * @returns True if the value is a data point reference
   */
  function isDataPointReference(value: unknown): value is DataPointTypeReference {
    return (
      typeof value === 'object' &&
      value !== null &&
      'id' in value &&
      'ref' in value &&
      typeof (value as DataPointTypeReference).id === 'string' &&
      typeof (value as DataPointTypeReference).ref === 'string'
    );
  }

  /**
   * Check if a value is a plain object (not null, not array).
   * 
   * @param value - The value to check
   * @returns True if the value is a plain object
   */
  function isObject(value: unknown): boolean {
    return typeof value === 'object' && value !== null && !Array.isArray(value);
  }

  /**
   * Convert a schema key to a human-readable label.
   * Uses the humanizeStringOrNumber utility for consistent formatting.
   * 
   * @param key - The schema property key
   * @returns Human-readable label
   */
  function humanizeKey(key: string): string {
    return humanizeStringOrNumber(key);
  }
  
  /**
   * Extract all unique data point type IDs from a parsed schema tree.
   * Recursively traverses sections to find all data points.
   * 
   * @param nodes - Array of schema nodes to extract from
   * @returns Set of unique data point type IDs
   */
  function extractDataPointIds(nodes: ParsedSchemaNode[]): Set<string> {
    const ids = new Set<string>();
    
    for (const node of nodes) {
      if (node.type === 'dataPoint') {
        ids.add(node.dataPointTypeId);
      } else if (node.type === 'section') {
        // Recursively extract from children
        const childIds = extractDataPointIds(node.children);
        childIds.forEach(id => ids.add(id));
      }
    }
    
    return ids;
  }
  
  /**
   * Batch load data point details in groups to avoid API overload.
   * Uses Promise.allSettled to handle partial failures gracefully.
   * Implements timeout per batch and caching.
   * 
   * @param dataPointIds - Array of data point IDs to load
   * @param batchSize - Number of requests per batch (default: 15)
   * @returns Map of successfully loaded data point details
   */
  async function batchLoadDataPointDetails(
    dataPointIds: string[],
    batchSize: number = 15
  ): Promise<Map<string, DataPointTypeSpecification>> {
    if (!fetchDataPointDetails) {
      return new Map();
    }
    
    const results = new Map<string, DataPointTypeSpecification>();
    const idsToFetch = dataPointIds.filter(id => !dataPointCache.value.has(id));
    
    // Return cached results if all IDs are cached
    if (idsToFetch.length === 0) {
      dataPointIds.forEach(id => {
        const cached = dataPointCache.value.get(id);
        if (cached) results.set(id, cached);
      });
      return results;
    }
    
    // Load in batches
    for (let i = 0; i < idsToFetch.length; i += batchSize) {
      const batch = idsToFetch.slice(i, i + batchSize);
      
      // Create timeout promise for this batch (10 seconds)
      const timeoutPromise = new Promise<never>((_, reject) => {
        setTimeout(() => reject(new Error('Batch request timeout')), 10000);
      });
      
      try {
        // Race between batch requests and timeout
        const batchPromise = Promise.allSettled(
          batch.map(id => fetchDataPointDetails(id))
        );
        
        const batchResults = await Promise.race([batchPromise, timeoutPromise]);
        
        // Process results
        batchResults.forEach((result, index) => {
          const id = batch[index];
          if (typeof id !== 'string') return; // Skip if id is not a string
          
          if (result.status === 'fulfilled') {
            const detail = result.value;
            results.set(id, detail);
            // Cache for future use
            dataPointCache.value.set(id, detail);
          } else {
            console.warn(`Failed to load data point ${id}:`, result.reason);
          }
        });
      } catch (err) {
        console.warn(`Batch loading timeout or error for batch starting at index ${i}:`, err);
        // Continue with next batch even if this one times out
      }
    }
    
    // Also include cached results for requested IDs
    dataPointIds.forEach(id => {
      if (!results.has(id)) {
        const cached = dataPointCache.value.get(id);
        if (cached) results.set(id, cached);
      }
    });
    
    return results;
  }
  
  /**
   * Enrich the parsed schema with data point details from the API.
   * Mutates the ParsedDataPoint nodes in-place to add enriched fields.
   * 
   * @param nodes - Schema nodes to enrich
   */
  async function enrichSchemaWithDataPointDetails(nodes: ParsedSchemaNode[]): Promise<void> {
    try {
      isLoadingDataPointDetails.value = true;
      
      // Extract all unique data point IDs
      const dataPointIds = Array.from(extractDataPointIds(nodes));
      
      if (dataPointIds.length === 0) {
        return;
      }
      
      // Batch load details
      const detailsMap = await batchLoadDataPointDetails(dataPointIds);
      
      // Enrich nodes recursively
      enrichNodesWithDetails(nodes, detailsMap);
    } catch (err) {
      console.error('Error enriching schema with data point details:', err);
      // Don't throw - degrade gracefully
    } finally {
      isLoadingDataPointDetails.value = false;
    }
  }
  
  /**
   * Recursively enrich nodes with loaded data point details.
   * Mutates ParsedDataPoint nodes to add enriched fields.
   * 
   * @param nodes - Nodes to enrich
   * @param detailsMap - Map of data point ID to details
   */
  function enrichNodesWithDetails(
    nodes: ParsedSchemaNode[],
    detailsMap: Map<string, DataPointTypeSpecification>
  ): void {
    for (const node of nodes) {
      if (node.type === 'dataPoint') {
        const details = detailsMap.get(node.dataPointTypeId);
        if (details) {
          node.businessDefinition = details.businessDefinition;
          node.dataPointName = details.name;
          node.baseTypeId = details.dataPointBaseType.id;
        }
      } else if (node.type === 'section') {
        // Recursively enrich children
        enrichNodesWithDetails(node.children, detailsMap);
      }
    }
  }

  return {
    // State
    frameworks: frameworks as Ref<SimpleFrameworkSpecification[]>,
    selectedFramework: selectedFramework as Ref<FrameworkSpecificationWithParsedSchema | null>,
    isLoadingFrameworks,
    isLoadingSpecification,
    isLoadingDataPointDetails,
    error,

    // Methods
    loadFrameworks,
    selectFramework,
  };
}
