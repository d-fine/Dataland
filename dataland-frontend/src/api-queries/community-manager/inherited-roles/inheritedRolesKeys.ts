export const inheritedRolesKeys = {
  all: ['inheritedRoles'] as const,
  byUserId: (keycloakUserId: string | undefined) => ['inheritedRoles', 'byUserId', keycloakUserId] as const,
};
