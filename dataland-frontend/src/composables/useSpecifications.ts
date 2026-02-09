import { ref, shallowRef, type Ref } from 'vue';
import type { SimpleFrameworkSpecification, FrameworkSpecification } from '@clients/specificationservice';
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
export function useSpecifications({ fetchFrameworks, fetchSpecification }: UseSpecificationsOptions) {
  // Reactive state
  const frameworks = ref<SimpleFrameworkSpecification[]>([]);
  const selectedFramework = shallowRef<FrameworkSpecificationWithParsedSchema | null>(null);
  const isLoadingFrameworks = ref(false);
  const isLoadingSpecification = ref(false);
  const error = ref<string | null>(null);

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

  return {
    // State
    frameworks: frameworks as Ref<SimpleFrameworkSpecification[]>,
    selectedFramework: selectedFramework as Ref<FrameworkSpecificationWithParsedSchema | null>,
    isLoadingFrameworks,
    isLoadingSpecification,
    error,

    // Methods
    loadFrameworks,
    selectFramework,
  };
}
