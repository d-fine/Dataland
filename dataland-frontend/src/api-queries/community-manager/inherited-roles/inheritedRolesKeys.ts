export const inheritedRolesKeys = {
  all: ['inheritedRoles'] as const,
  byUserId: (keycloakUserId: string | undefined) => [...inheritedRolesKeys.all, 'byUserId', keycloakUserId] as const,
};
