# Test Fixtures for Specification Viewer

This directory contains JSON fixtures for testing the specification viewer components and composables.

## Fixtures Overview

### Framework Lists

- **framework-list.json**: Array of framework summaries (SimpleFrameworkSpecification[])
  - Contains 3 frameworks: LkSG, SFDR, and EU Taxonomy
  - Used for testing framework selection dropdown and list display

### Framework Specifications

- **lksg-framework.json**: Complete LkSG framework with parsed schema
  - Contains typical structure with nested sections and data points
  - Schema includes: general info, company info, risk management sections
  - ~7 data points across multiple nesting levels
  - Use case: Standard framework rendering, integration tests

- **empty-framework.json**: Framework with empty schema object
  - Use case: Testing empty state handling, edge case validation

- **nested-schema.json**: Framework with deeply nested structure (5 levels)
  - Single data point at level 5 depth
  - Use case: Testing recursive rendering limits, performance

### Data Point Details

- **datapoint-details.json**: Standard data point details (plainString type)
  - Includes: name, business definition, base type, constraints, using frameworks
  - Use case: Testing modal content display, standard data point

- **datapoint-with-constraints.json**: Data point with complex constraints (plainInteger type)
  - Multiple detailed constraints
  - Use case: Testing constraint formatting and display

## Usage in Tests

### Component Tests

```typescript
cy.fixture('specifications/framework-list.json').then((frameworks) => {
  cy.intercept('GET', '/specifications/frameworks', frameworks).as('getFrameworks')
})
```

### Inline Intercept

```typescript
cy.intercept('GET', '/specifications/frameworks', { 
  fixture: 'specifications/framework-list.json' 
}).as('getFrameworks')
```

### Direct Import (for unit tests)

```typescript
import frameworkList from '@/../testing/data/specifications/framework-list.json'
```

## Schema Structure

The schema JSON string within framework objects follows this structure:

- **Sections**: Plain objects containing nested structure (e.g., `"general": {...}`)
- **Data Points**: Objects with required properties:
  - `id`: Unique data point type ID
  - `ref`: Base type reference
  - `aliasExport`: Human-readable display name (optional, falls back to key)

Example:
```json
{
  "section": {
    "subsection": {
      "dataPointKey": {
        "id": "unique_id",
        "ref": "plainString",
        "aliasExport": "Display Name"
      }
    }
  }
}
```

## Extending Fixtures

When adding new fixtures:
1. Follow naming convention: `{purpose}-{type}.json`
2. Ensure JSON is valid and properly formatted
3. Update this README with fixture description and use cases
4. Add TypeScript types if introducing new structure
