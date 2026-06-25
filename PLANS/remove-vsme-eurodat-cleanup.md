# Remove VSME Framework And EuroDaT Runtime Cleanup

## Goal

Remove the VSME framework, VSME-specific tests and test data sources, EuroDaT setup/runtime references, and the targeted docker compose containers while keeping remaining tests green.

## User Instructions

- Run gradle and npm tasks with the @command-summarizer subagent

## Non-Negotiable Rule For Generated Files

- [ ] Do not manually edit generated OpenAPI JSON files.
- [ ] Do not manually edit generated clients.
- [ ] Do not manually edit generated fixture outputs under `testing/data`.
- [ ] Do not manually patch generated framework artifacts if they are produced by `dataland-framework-toolbox`.
- [ ] Fix source-of-truth inputs, generator registrations, and consuming source code, then run the relevant generation commands.
- [ ] If stale generated files remain after generation, fix the generator cleanup/source configuration or document the generator gap before touching generated outputs.

## Scope Decisions Before Coding

- [x] Confirm the exact three docker compose services intended by the ticket.
- [x] Initial search found likely EuroDaT services in `docker-compose.yml`: `eurodat-client`, `dummy-eurodat-client`, `dummy-eurodat-db`.
- [x] Decide whether `external-storage` is also in scope. The user confirmed that external storage should be removed too.
- [x] Recommended default was overridden by explicit scope confirmation: remove the three EuroDaT services and `external-storage` together.
- [ ] Decide persisted-data handling for existing `vsme` rows. Do not edit old Flyway migrations. Add a new migration only if production/staging data must be deleted, migrated, or made non-queryable.

## Phase 1: Baseline And Inventory

- [x] Read root `AGENTS.md` and package-specific `AGENTS.md` files before editing touched areas.
- [x] Run `git status --short` and note unrelated worktree changes without reverting them.
- [x] Search current references with `rg --hidden -g '!.git' -n "vsme|VSME|EuroDaT|Eurodat|eurodat|external-storage|externalStorage|EXTERNAL_STORAGE|IGNORE_EXTERNAL_STORAGE|CYPRESS_IGNORE_EXTERNAL_STORAGE|INTERNAL_EURODATCLIENT|DATALAND_DUMMY_EURODAT|DATALAND_EXTERNAL_STORAGE"`.
- [x] Record references that are intentionally allowed to remain, such as immutable historical Flyway migrations if applicable.
- [x] Load `dataland-run-framework-toolbox`, `dataland-openapi-impact`, and `dataland-frontend-impact` skills before executing generator-heavy phases.

## Phase 1 Inventory Notes

- `git status --short` already showed unrelated worktree changes before cleanup work: modified `AGENTS.md`, modified `opencode.jsonc`, and untracked `HURL.md`, `MeldebögenAbgleich(1).xlsx`, `PLANS/DALA-6985.md`, `PLANS/DALA-7013.md`, this plan file, several `.hurl` files, and several `.env` helper files.
- Hidden-inclusive search counts at baseline: 107 files with `vsme|VSME`, 46 files with EuroDaT/eurodat terms, and 32 files with external-storage terms.
- Main VSME reference groups: framework toolbox inputs/implementation, generated backend framework/model/controller sources, backend API/controller/private-data code, backend and consumer tests, frontend framework/upload/view/Cypress/component-test code, E2E tests/helpers, generated OpenAPI specs, and generated fake fixture outputs under `testing/data`.
- Main EuroDaT reference groups: `docker-compose.yml`, environment files, deployment scripts, build scripts, dummy EuroDaT module, EuroDaT client secret/spec files, external-storage implementation, backend private-storage mapping code, frontend E2E conditional behavior, website logo/config, `.gitignore`, and architecture/maintenance documentation.
- Main external-storage reference groups: `docker-compose.yml`, `.github` CI/CD workflows, environment files, `settings.gradle.kts`, backend generated-client wiring and private-data code, inbound proxy locations, E2E wait/run scripts, frontend Cypress config/support, external-storage module files, build scripts, and OpenCode skills that mention generation workflows.
- Compose services present at baseline: `eurodat-client`, `dummy-eurodat-client`, `dummy-eurodat-db`, and separate `external-storage`. The first three match the likely ticket target; `external-storage` remains a separate scope decision.
- Intentionally allowed remaining-reference categories during removal: immutable historical Flyway migrations such as `dataland-community-manager/src/main/kotlin/db/migration/V8__AddAccessStatusToRequestStatusHistory.kt`, generated OpenAPI JSON until regenerated, generated clients until regenerated, generated fixture outputs under `testing/data` until regenerated, this cleanup plan, and out-of-scope historical documentation until final documentation cleanup.

## Phase 2: Remove VSME From Framework Source Of Truth

- [x] Remove VSME framework source input from `dataland-framework-toolbox/inputs/vsme/`.
- [x] Remove VSME framework implementation from `dataland-framework-toolbox/src/main/kotlin/org/dataland/frameworktoolbox/frameworks/vsme/`.
- [x] Remove VSME registration/import references such as `VsmeFramework`.
- [x] Run `./gradlew :dataland-framework-toolbox:runCreateFrameworkList`.
- [x] Run `./gradlew :dataland-framework-toolbox:test`.
- [x] Run `./gradlew :dataland-framework-toolbox:integrationTest` if framework loading/generation behavior changed.
- [x] Do not run or keep `runCoverage --args='vsme'` as a required check after VSME is removed.

## Phase 2 Notes

- Deleted the VSME toolbox input files and VSME toolbox Kotlin package. No `vsme|VSME|Vsme` references remain in `dataland-framework-toolbox`.
- `runCreateFrameworkList` succeeded and printed a framework list without `vsme`: `eutaxonomy-financials`, `eutaxonomy-financials-2026-73`, `eutaxonomy-non-financials`, `eutaxonomy-non-financials-2026-73`, `lksg`, `nuclear-and-gas`, `pcaf`, `sfdr`.
- `:dataland-framework-toolbox:test` succeeded.
- `:dataland-framework-toolbox:integrationTest` succeeded, but it writes integration-test framework artifacts into the repository by design. The generated `integrationTesting` files were removed after the run.
- Generator cleanup gap: a follow-up `runCoverage --args='sfdr'` initially failed before registry regeneration because stale generated `integrationTesting` registry imports referenced files removed during integration-test cleanup. The stale `integrationTesting` registry entries were removed to unblock generation.
- A rerun of `runCoverage --args='sfdr'` succeeded and completed framework registry generation, fake fixture generation, and frontend typecheck. It cleaned the stale registry and SFDR fixture side effects.
- `dataland-backend/backendOpenApi.json` still has a small generated property-order diff from OpenAPI regeneration. It is not a VSME removal and should be resolved by the later OpenAPI generation phase, not by manual JSON editing.

## Phase 3: Remove Backend VSME API And Private Data Flow

- [x] Remove source references to `VsmeDataApi`, `/data/vsme`, and VSME-specific controller contracts.
- [x] Remove VSME-specific custom backend model classes if they are no longer generated or referenced.
- [x] Remove VSME-specific methods from `PrivateDataManager`, including private VSME upload/retrieval logic.
- [x] Remove hardcoded `metaInfo.dataType == "vsme"` behavior from `MetaDataController`.
- [x] Remove VSME examples from shared Swagger/example utilities.
- [x] Update backend tests that currently assert VSME is a valid data type.
- [x] Delete tests that only validate retired VSME behavior.
- [x] Adjust generic tests to use a remaining framework when the test purpose is not VSME-specific.

## Phase 3 Notes

- Added framework-toolbox cleanup for removed generated framework output directories. The cleanup removes inactive generated backend/frontend framework directories and fixture-generator framework directories, then refreshes frontend framework registry imports. This was needed because deleting VSME from the toolbox source of truth left stale generated `dataland-backend/.../frameworks/vsme`, `dataland-frontend/src/frameworks/vsme`, and frontend fake-fixture sources until generation ran again.
- The cleanup keeps `generatedFileWarning = "THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX"` as a safety marker and only deletes inactive directories that contain toolbox-generated files. Frontend `custom/` is explicitly ignored.
- `./gradlew :dataland-framework-toolbox:test` initially failed due a missing `java.io.File` import after the cleanup change; the import was restored and the test passed via `@command-summarizer`.
- `./gradlew :dataland-framework-toolbox:runCoverage --args='sfdr'` then removed stale generated VSME backend/frontend output and exposed real compile errors in hand-written source. Those were fixed by deleting `VsmeDataApi`, deleting the VSME multipart converter, removing VSME upload/retrieval methods from `PrivateDataManager`, removing `PrivateDataAccessChecker.hasUserAccessToAtLeastOnePrivateResourceForCompany`, removing the VSME metadata patch guard, and updating backend data-type tests.
- `dataland-backend/src/main/kotlin` and `dataland-backend/src/test/kotlin` focused searches for VSME source references were clean after Phase 3, excluding generated OpenAPI JSON and later consumer modules.

## Phase 4: Remove VSME From Backend Consumer Services

- [x] Regenerate backend clients in dependent services after backend contract generation.
- [x] Remove `DataTypeEnum.vsme` handling from `dataland-community-manager`.
- [x] Review and adjust `DataAccessManager`, `DataRequestUpdateManager`, `DataRequestNonSourceabilityManager`, `SingleDataRequestManager`, and related tests.
- [x] Preserve generic data-request coverage by switching tests to a remaining framework where appropriate.
- [x] Remove VSME display mapping from `dataland-user-service`.
- [x] Update `dataland-qa-service` tests that use `DataTypeEnum.vsme`; use another data type if the test is about framework-scoped preapproval behavior.
- [x] Regenerate affected service OpenAPI specs from source, not by editing JSON.

## Phase 4 Notes

- `runCoverage --args='sfdr'` exposed regenerated-client failures where `DataTypeEnum.vsme` no longer exists in `dataland-community-manager` and `dataland-user-service`.
- Removed VSME display mappings from `dataland-community-manager` and `dataland-user-service`.
- Removed VSME-only private data request branching from `DataAccessManager`, `DataRequestNonSourceabilityManager`, `DataRequestUpdateManager`, `SingleDataRequestManager`, and `CommunityManagerDataRequestProcessingUtils`. Since VSME was the only private framework in the registry, generic public request behavior remains.
- Deleted `dataland-community-manager/src/test/kotlin/org/dataland/datalandcommunitymanager/services/DataAccessManagerTest.kt` because it tested retired VSME private-access behavior only.
- Switched several generic community-manager and QA tests from VSME to remaining frameworks (`pcaf`, `lksg`, or `sfdr`) where the test purpose was not VSME-specific.
- Focused searches in `dataland-community-manager/src/test/kotlin`, `dataland-qa-service/src/test/kotlin`, and `dataland-user-service/src/test/kotlin` are clean for VSME source references.
- `./gradlew :dataland-community-manager:test`, `./gradlew :dataland-qa-service:test`, and `./gradlew :dataland-user-service:test` passed via `@command-summarizer` after the test cleanup.

## Phase 5: Remove Frontend VSME Source References

- [x] Remove VSME upload wiring from `UploadFormWrapper.vue`.
- [x] Remove `CreateVsmeDataset.vue` if no longer referenced.
- [x] Remove VSME-specific view fallback behavior from `ViewFrameworkBase.vue`.
- [x] Update `Constants.ts` so framework lists no longer include VSME.
- [x] Update `Frameworks.ts` for empty/no private framework behavior.
- [x] Update `GenericFrameworkTypes.ts` after generated clients no longer expose `VsmeData`.
- [x] Remove VSME-specific dropdown helpers from `PremadeDropdownDatasets.ts` if unused.
- [x] Remove VSME display converter references from `MultiLayerDataTableFieldConverter.ts`.
- [x] Remove VSME Cypress specs and remove `require('./vsme')` from the E2E spec index.
- [x] Remove `uploadVsmeFrameworkData()` from Cypress utilities.
- [x] Adjust prepopulation specs so they no longer upload or count VSME fixtures.
- [x] Adjust component tests that hardcode framework count, VSME label, VSME cockpit panels, or VSME mock data.

## Phase 5 Notes

- `runCoverage --args='sfdr'` failed once in `:dataland-frontend:npm_run_checkfakefixturecompilation` because stale generated registry imports and fake-fixture sources still referenced VSME. The toolbox cleanup was extended to remove inactive frontend fixture-generator directories and refresh registry imports before framework compilation.
- Deleted VSME upload/view/form source files and VSME-only form-field components: `CreateVsmeDataset.vue`, VSME custom list subform elements, VSME custom form-field wrappers, and `VsmeDisplayValueGetters.ts`.
- Removed VSME from upload routing, private/public framework constants, framework editability/private checks, generic framework union types, prepopulation, Cypress upload helpers, and component tests.
- Replaced the removed generated `BaseDataPointString` frontend client type with a local structural type in `ListOfBaseDataPointGetterFactory.ts` after regenerated clients no longer exported it.
- Latest `./gradlew :dataland-framework-toolbox:runCoverage --args='sfdr'` passed via `@command-summarizer` after these fixes. It completed generated clients, fake fixtures, `checkfakefixturecompilation`, and frontend typecheck without compiler/typecheck errors.
- Focused searches in `dataland-frontend/src` and `dataland-frontend/tests` were clean for VSME references after the passing run, excluding already-deleted tracked files shown as `D` in git status.

## Phase 6: Remove VSME E2E And Fixture Sources

- [x] Remove Kotlin E2E VSME tests under `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/frameworks/Vsme.kt`.
- [x] Remove VSME E2E helpers such as `VsmeTestUtils.kt` and `CustomVsmeDataControllerApi.kt`.
- [x] Remove VSME fixture provider mappings from `FrameworkTestDataProvider.kt`.
- [x] Remove VSME fake fixture source inputs through the generator/toolbox source path.
- [x] Do not manually edit `testing/data/CompanyInformationWithVsmeData.json`.
- [x] Do not manually edit `testing/data/CompanyInformationWithVsmePreparedFixtures.json`.
- [x] Do not manually edit `testing/data/DataRequestsMock.json` or framework summary fixture outputs.
- [x] Run fake fixture generation after source cleanup.

## Phase 6 Notes

- Deleted Kotlin VSME E2E test/helper/API-controller sources and removed the VSME fixture-provider mapping.
- Deleted frontend E2E VSME specs and Cypress upload helper references.
- VSME frontend fake-fixture source directory was removed by toolbox generation cleanup, not by manual generated-output editing.
- `runCoverage --args='sfdr'` completed `npm_run_fakefixtures`; `testing/data/DataRequestsMock.json` changed as a generated side effect. Do not manually edit generated `testing/data` outputs; use fake-fixture generation/verification.
- Finished cleaning generic Kotlin E2E request-query tests by switching `QueryDataRequestsCountingTests.kt` and `UserServiceTest.kt` from VSME to PCAF and fixing the already converted mixed PCAF/LkSG assertion in `QueryDataRequestsTest.kt`.
- Focused searches in `dataland-e2etests/src/test/kotlin` are clean for VSME source references.
- `./gradlew :dataland-e2etests:compileTestKotlin` passed via `@command-summarizer` after the cleanup.
- `npm --prefix ./dataland-frontend run checkcypresscompilation` passed via `@command-summarizer` after deleting VSME Cypress specs/helpers.

## Compact Handoff: Current State On 2026-06-24

- User instruction: all Gradle and npm tasks must be run through the `@command-summarizer` subagent. Do not run high-output Gradle/npm commands directly in the main thread.
- Current implementation has completed Phase 7 and Phase 8 source/config removal. Remaining work is broader generated-file verification and any additional scoped checks the PR owner wants before finalizing.
- Recent successful commands via `@command-summarizer`: `./gradlew :dataland-framework-toolbox:runCoverage --args='sfdr'`, `./gradlew :dataland-e2etests:compileTestKotlin`, `./gradlew :dataland-community-manager:test`, `./gradlew :dataland-qa-service:test`, `./gradlew :dataland-user-service:test`, `npm --prefix ./dataland-frontend run checkcypresscompilation`, `./gradlew :dataland-backend:compileKotlin`, `./gradlew :dataland-backend:generateOpenApiDocs`, `./gradlew :dataland-internal-storage:generateClients`, `./gradlew :dataland-frontend:generateClients`, `./gradlew :dataland-e2etests:generateClients`, `./gradlew :dataland-internal-storage:compileKotlin :dataland-internal-storage:compileTestKotlin`, `./gradlew :dataland-message-queue-utils:test`, `./gradlew :dataland-community-manager:test`, Docker Compose config checks for `testing`, `production`, and `development`, `./gradlew projects`, and `./gradlew :dataland-backend:compileKotlin :dataland-community-manager:compileKotlin :dataland-message-queue-utils:compileKotlin`.
- Additional successful commands via `@command-summarizer`: `./gradlew :dataland-backend:generateClients :dataland-frontend:generateClients :dataland-e2etests:generateClients`, `./gradlew :dataland-backend:compileKotlin :dataland-internal-storage:compileKotlin :dataland-internal-storage:compileTestKotlin :dataland-e2etests:compileTestKotlin :dataland-community-manager:test :dataland-message-queue-utils:test`, and frontend `typecheck`, `checkfakefixturecompilation`, and `checkcypresscompilation`.
- User manually reran `./gradlew :dataland-backend:test` after an overlapping SFDR regeneration completed and reported that it succeeds.
- Important changed generator behavior: `FrameworkToolboxCli.cleanRemovedFrameworkOutputs()` now runs for `all` and single-framework generation, removes inactive generated backend/frontend/fixture framework directories, refreshes frontend registry imports, and uses `generatedFileWarning` as a safety marker. `PavedRoadFramework` now exposes `backendPackageName` using `removeUnallowedJavaIdentifierCharacters(identifier)`.
- Generated files were not manually edited. Changes to `dataland-backend/backendOpenApi.json`, `dataland-qa-service/qaServiceOpenApi.json`, frontend registry imports, generated deleted VSME framework directories, and `testing/data/DataRequestsMock.json` are generation side effects.
- Focused frontend source/test search was clean after the passing generation run: no VSME hits in `dataland-frontend/src` or `dataland-frontend/tests`, except deleted tracked files shown as `D` by git status before the search state was updated.
- Known intentionally allowed VSME references for now: historical Flyway migration `dataland-community-manager/src/main/kotlin/db/migration/V8__AddAccessStatusToRequestStatusHistory.kt`, this plan, generated artifacts until final verification, and out-of-scope docs until final cleanup.
- Final EuroDaT/external-storage semantic searches currently only hit this cleanup plan.

## Immediate Next Steps After Compact

- Continue with broader generated-file verification and final PR cleanup. Phase 7/8 source/config removal is complete.

## Phase 7: Remove EuroDaT Docker, Env, GitHub, And Deployment References

- [x] Remove selected compose services from `docker-compose.yml`.
- [x] Remove unused `x-dummy-eurodat-db-settings` anchor if no service uses it.
- [x] Remove `INTERNAL_EURODATCLIENT_URL` from environment files.
- [x] Remove EuroDaT credential variables from `.env.dev` and `.env.template`.
- [x] Remove `CYPRESS_IGNORE_EXTERNAL_STORAGE` and `IGNORE_EXTERNAL_STORAGE_ERROR` only after confirming no remaining test/runtime path uses external storage.
- [x] Remove EuroDaT secret plumbing from `.github/workflows/CD.yaml`.
- [x] Remove external-storage CI test task from `.github/workflows/CI.yaml` only if `dataland-external-storage` is removed.
- [x] Remove EuroDaT secret-file setup from `deployment/start_and_deploy_to_server.sh`.
- [x] Remove `eurodat-client` healthcheck exclusions from `deployment/docker_utils.sh`.
- [x] Remove EuroDaT build args from `build-utils/base_rebuild_single_docker_image.sh` if their images are removed.
- [x] Remove obsolete rebuild scripts for dummy EuroDaT or external storage images if their modules are removed.
- [x] Remove obsolete `.gitignore` EuroDaT secret-file entries.
- [ ] Ask a repo admin to delete obsolete GitHub secrets after workflow references are gone.

## Phase 7 Notes

- Removed `external-storage`, `eurodat-client`, `dummy-eurodat-client`, and `dummy-eurodat-db` from `docker-compose.yml`, along with the dummy EuroDaT DB settings anchor.
- Removed EuroDaT/external-storage environment variables and GitHub workflow plumbing, including the CD input for ignoring external-storage startup errors.
- Removed deployment secret-file setup, `eurodat-client` healthcheck exclusion, EuroDaT/external-storage image rebuild scripts and build args, and stale `.gitignore` secret-file entries.
- Removed external-storage proxy routes and E2E wait/JaCoCo service references.
- Docker Compose config checks for `testing`, `production`, and `development` passed via `@command-summarizer`; removed services were absent from rendered services.

## Phase 8: Optional External Storage Module Removal

- [x] Execute this phase only if `external-storage` is confirmed in scope or no remaining consumer exists.
- [x] Remove `dataland-external-storage` from `settings.gradle.kts`.
- [x] Remove `dataland-dummy-eurodat-client` from `settings.gradle.kts` if removed.
- [x] Remove backend external-storage generated-client tasks from `dataland-backend/build.gradle.kts`.
- [x] Remove backend service classes that call external storage.
- [x] Remove external-storage base URLs from backend properties.
- [x] Remove external-storage proxy locations.
- [x] Remove external-storage health waits from E2E scripts.
- [x] Remove external-storage from JaCoCo/service collection scripts.
- [x] Remove private-data RabbitMQ exchanges only after confirming no backend/community-manager listener still uses them.
- [x] Remove `dataland-eurodat-client`, `dataland-dummy-eurodat-client`, and `dataland-dummy-eurodat-db` directories if no remaining build path references them.

## Phase 8 Notes

- Removed tracked `dataland-external-storage`, `dataland-eurodat-client`, `dataland-dummy-eurodat-client`, and `dataland-dummy-eurodat-db` files.
- Removed backend external-storage generated-client generation and source-set wiring.
- Removed backend private temporary-cache endpoints, private-data manager/listener, external-storage data getter/streaming API, EuroDaT mapping entity/repository/key, and external-storage URL properties.
- Removed private-data RabbitMQ constants/message class and the community-manager listener/tests that processed private-storage completion messages.
- Removed frontend Cypress external-storage skip flag and EuroDaT-live conditional test wrapper behavior.
- Removed EuroDaT website logo/config, PR maintenance checklist, OpenCode skill module references, and EuroDaT/external-storage nodes from the architecture diagram.
- `./gradlew projects` passed via `@command-summarizer`; `dataland-external-storage` and `dataland-dummy-eurodat-client` are no longer listed.

## Phase 9: Generation Sequence

- [x] Run framework toolbox generation first.
- [x] Run `./gradlew :dataland-backend:generateOpenApiDocs`.
- [x] Run `./gradlew :dataland-community-manager:generateClients`.
- [x] Run `./gradlew :dataland-qa-service:generateClients`.
- [x] Run `./gradlew :dataland-user-service:generateClients`.
- [x] Run `./gradlew :dataland-data-sourcing-service:generateClients`.
- [x] Fix source compile errors in those services.
- [x] Regenerate affected service OpenAPI specs after source fixes.
- [x] Run `./gradlew :dataland-frontend:generateClients`.
- [x] Run `./gradlew :dataland-e2etests:generateClients`.
- [x] Run `./gradlew :dataland-frontend:npm_run_fakefixtures`.
- [x] Run `testing/verifyOpenApiFiles.sh`.
- [x] Run `testing/verify_that_fake_fixtures_are_up_to_date.sh` and document the expected generated diff failure.

## Phase 9 Notes

- `./gradlew :dataland-backend:generateOpenApiDocs` passed and updated `dataland-backend/backendOpenApi.json` after removing private temporary-cache endpoints.
- Regenerated clients for `dataland-internal-storage`, `dataland-frontend`, and `dataland-e2etests`; all passed.
- `testing/verifyOpenApiFiles.sh` first timed out at the default 120-second summarizer run while processing backend. A longer rerun completed and reported only `dataland-community-manager/communityManagerOpenApi.json` stale; the verification run regenerated it.
- After the community-manager OpenAPI update, downstream client generation for backend, frontend, and E2E passed.
- The shared bulk data-type OpenAPI example was also updated to remove `vsme`, then `dataland-data-sourcing-service` and `dataland-community-manager` specs were regenerated.
- `./gradlew :dataland-framework-toolbox:runCreateFrameworkList` passed and listed the remaining frameworks without `vsme`.
- `./gradlew :dataland-qa-service:generateClients :dataland-user-service:generateClients :dataland-data-sourcing-service:generateClients` passed; all tasks were up-to-date on the final rerun.
- A final `testing/verifyOpenApiFiles.sh` run passed after an initial transient port `8483` conflict between OpenAPI generation tasks.
- `testing/verify_that_fake_fixtures_are_up_to_date.sh` completed fixture generation successfully but failed its final `git diff --exit-code` check because generated fixture changes are expected versus `HEAD`, including removal of generated VSME fixture outputs. This is a generated side effect to include, not a hand edit.

## Phase 10: Verification

- [x] Run `./gradlew :dataland-framework-toolbox:test`.
- [x] Run `./gradlew :dataland-backend:test`.
- [x] Run `./gradlew :dataland-community-manager:test`.
- [x] Run `./gradlew :dataland-qa-service:test`.
- [x] Run `./gradlew :dataland-user-service:test`.
- [x] Run `./gradlew :dataland-data-sourcing-service:test`.
- [x] Run `./gradlew :dataland-e2etests:compileTestKotlin`.
- [x] Run `npm --prefix ./dataland-frontend run lint`.
- [x] Run `npm --prefix ./dataland-frontend run typecheck`.
- [x] Run `npm --prefix ./dataland-frontend run checkcypresscompilation`.
- [x] Run `npm --prefix ./dataland-frontend run checkfakefixturecompilation`.
- [x] Run `npm --prefix ./dataland-frontend run lintci`.
- [x] Run `npm --prefix ./dataland-frontend run formatci`.
- [x] Run `npm --prefix ./dataland-frontend run checkdependencies`.
- [x] Run `docker compose --profile testing config`.
- [x] Run `docker compose --profile production config`.
- [x] Run `docker compose --profile development config`.
- [x] Run `./gradlew ktlintCheck detekt` before finishing if production Kotlin changed.

## Phase 10 Notes

- `./gradlew :dataland-backend:test` was manually rerun by the user after an overlapping SFDR regeneration finished and was reported successful.
- Scoped backend and consumer checks passed via `@command-summarizer`: backend/internal-storage/E2E compiles, community-manager tests, and message-queue-utils tests.
- Additional scoped checks passed after final cleanup: `:dataland-backend-utils:compileKotlin`, `:dataland-data-sourcing-service:compileKotlin`, `:dataland-community-manager:test`, and `:dataland-email-service:test`.
- `./gradlew :dataland-data-sourcing-service:test` passed via `@command-summarizer`.
- Frontend `typecheck`, `checkfakefixturecompilation`, and `checkcypresscompilation` passed via `@command-summarizer` after regeneration.
- Frontend `lint`, `lintci`, `formatci`, and `checkdependencies` passed via `@command-summarizer` after removing cleanup-introduced unused symbols and formatting the affected files.
- `./gradlew ktlintCheck detekt` passed via `@command-summarizer` after removing cleanup-introduced unused imports/parameters and formatting the affected toolbox call chain.
- Website `typecheck` and `build` passed via `@command-summarizer` after removing the EuroDaT logo/config.
- Docker Compose config checks passed for `testing`, `production`, and `development`; removed services were absent.

## Phase 11: Final Searches And Documentation

- [x] Run final semantic search for `vsme|VSME`.
- [x] Run final semantic search for `EuroDaT|Eurodat|eurodat`.
- [x] Run final semantic search for `external-storage|externalStorage|EXTERNAL_STORAGE|IGNORE_EXTERNAL_STORAGE|CYPRESS_IGNORE_EXTERNAL_STORAGE|INTERNAL_EURODATCLIENT`.
- [x] Confirm remaining hits are intentional historical migrations, generated side effects, or out-of-scope documentation.
- [x] Update repo documentation such as architecture diagrams, maintenance notes, and PR templates if they mention EuroDaT setup.
- [ ] Update internal wiki pages for local setup, EuroDaT credentials, EuroDaT secrets, and deployment setup.
- [ ] If the internal wiki is outside this repo, create a follow-up task or PR note with exact pages to update.

## Test Removal Rationale To Include In PR

- [x] VSME upload/view/access tests are removed because the framework and its private-data flow are retired.
- [x] VSME fixture tests are removed because generated VSME fixture sources are retired.
- [x] Generic data-request, preapproval, and admin overview tests are retained by switching to remaining frameworks where the behavior is still product-relevant.
- [x] EuroDaT-live conditional Cypress behavior is removed if no remaining test depends on external private storage.
- [x] Remaining tests pass after generated specs, clients, and fixtures are regenerated from source.

## Completion Criteria

- [x] VSME is not available in backend data types, frontend framework lists, upload routes, view routes, Cypress specs, or E2E framework providers.
- [x] The selected three docker compose containers are gone.
- [x] EuroDaT env vars and GitHub workflow references are gone.
- [x] Generated files are updated only via generation workflows.
- [x] OpenAPI verification passes.
- [ ] Fake fixture verification passes.
- [x] Remaining scoped tests pass.
- [x] Any intentionally remaining historical references are documented.

## Phase 12: Finalize CI Failure Fixes

- [x] Fix `FrameworkToolboxCli.buildSingleFramework()` so unknown framework validation happens before removed-output cleanup.
- [x] Preserve the `isFrameworkPrivate(framework: DataTypeEnum)` and `isFrameworkEditable(framework: DataTypeEnum)` function signatures in `dataland-frontend/src/utils/Frameworks.ts`.
- [x] Implement `isFrameworkPrivate` through the now-empty `PRIVATE_FRAMEWORKS` list and implement `isFrameworkEditable` as the inverse private-framework check.
- [x] Remove the stale third argument from the `validateFrameworkSummaryPanels(...)` call in `CompanyCockpitPage.cy.ts`.
- [x] Update `QueryDataRequestsTest.kt` so email-address assertions match the public-only data-request flow after VSME removal.
- [x] Keep admin email visibility for queried data requests and keep public request email masking for company owners/non-admin users.

## Phase 12 Notes

- CI failures currently map to three cleanup regressions: frontend typecheck failures from changed framework utility signatures and a stale Cypress helper call, framework-toolbox unit-test failure from cleanup running before unknown-framework validation, and an E2E assertion that still assumes a private VSME-style request path.
- Do not reintroduce VSME-specific branching to satisfy the tests. The fixes should preserve the post-removal behavior: all remaining single data requests are public, and no private framework is registered.
- Prefer restoring the existing frontend utility API over editing every source call site, because callers still naturally pass the framework being checked and future private frameworks can reuse the same API.
- Implemented `buildSingleFramework()` validation before cleanup so unknown frameworks fail with the intended `IllegalArgumentException` before touching generated outputs.
- Restored framework utility argument signatures and backed `isFrameworkPrivate` with `PRIVATE_FRAMEWORKS`; with no private frameworks left, `isFrameworkEditable` is true for all remaining frameworks.
- Updated the E2E data-request email test to assert admin visibility and public-request masking for company owners/non-admins after replacing VSME private requests with PCAF public requests.

## Phase 13: Final Verification After CI Fixes

- [x] Run `./gradlew :dataland-framework-toolbox:test` via `@command-summarizer`.
- [x] Run `./gradlew :dataland-frontend:npm_run_typecheck` via `@command-summarizer`.
- [x] Run `./gradlew :dataland-frontend:npm_run_checkcypresscompilation` via `@command-summarizer` if Cypress component/test TypeScript changed.
- [ ] Run `./gradlew :dataland-e2etests:test --tests org.dataland.e2etests.tests.communityManager.QueryDataRequestsTest` via `@command-summarizer`.
- [x] Run `./gradlew :dataland-framework-toolbox:integrationTest` via `@command-summarizer`.
- [x] Run at least one representative toolbox consistency check, such as `./gradlew :dataland-framework-toolbox:runCoverage --args='eutaxonomy-financials'`, via `@command-summarizer`.
- [x] After any generator-style toolbox run, run `./testing/verify_that_no_git_tracked_files_changed.sh` and verify any tracked changes are intentional.

## Phase 13 Notes

- The framework-toolbox consistency matrix invokes frontend typecheck internally, so rerun a representative `runCoverage` only after direct frontend typecheck passes.
- If the representative `runCoverage` still fails, fetch the exact diagnostic before changing generated outputs. The likely next causes would be stale generated registry imports or unintended tracked generator side effects.
- `:dataland-framework-toolbox:test`, `:dataland-frontend:npm_run_typecheck`, and `:dataland-frontend:npm_run_checkcypresscompilation` passed via `@command-summarizer` after the Phase 12 fixes.
- The targeted `QueryDataRequestsTest` command failed during class initialization with `java.net.ConnectException` at company setup, before any test method ran. This local environment does not have the required E2E service stack running, so the updated assertions were not executed.
- `:dataland-framework-toolbox:integrationTest` initially failed when run concurrently with `runCoverage` because transient `integrationTesting` fixture outputs conflicted with fake-fixture TypeScript compilation. Rerunning `integrationTest` by itself passed.
- `:dataland-framework-toolbox:runCoverage --args='eutaxonomy-financials'` passed once after the frontend checks. A later cleanup rerun regenerated normal outputs and reached frontend typecheck but the summarizer timed out before completion; no compiler or generator errors were reported before timeout.
- `./testing/verify_that_no_git_tracked_files_changed.sh` failed as expected because the script detects all current tracked worktree changes, including this Phase 12 source edit and pre-existing cleanup-plan changes. It also stages files internally; the staging was reverted with `git restore --staged .` without touching working-tree content.
- Transient `integrationTesting` generated source outputs were removed by a normal `runCoverage` cleanup. The two remaining untracked generated `testing/data/CompanyInformationWithIntegrationtesting*.json` files created by the integration test run were deleted because there is no corresponding active framework source.

## Phase 14: Final PR Readiness Check

- [ ] Re-run the focused semantic searches for `vsme|VSME`, `EuroDaT|Eurodat|eurodat`, and `external-storage|externalStorage|EXTERNAL_STORAGE|IGNORE_EXTERNAL_STORAGE|CYPRESS_IGNORE_EXTERNAL_STORAGE|INTERNAL_EURODATCLIENT`.
- [ ] Confirm remaining hits are still limited to historical migrations, this plan, generated side effects already accounted for, or out-of-scope documentation.
- [ ] Re-check fake fixture verification and decide whether the expected generated diff should be committed or whether generator cleanup is still incomplete.
- [ ] Update the completion criteria if fake fixture verification is now green.
- [ ] Include the final CI-failure rationale in the PR notes: VSME private-data behavior was removed, generic request coverage was retained with public frameworks, and generated files were updated only through generators.

## Phase 15: RabbitMQ Admin Queue CI Fix

- [x] Remove retired private-data queues from the RabbitMQ admin Cypress expectation.
- [x] Run `npm --prefix ./dataland-frontend run checkcypresscompilation` via `@command-summarizer`.

## Phase 15 Notes

- Removed `dataStoredBackendPrivateDataManager` and `privateRequestReceivedCommunityManager` from `RabbitMQAdmin.ts` because both queues belonged to the removed VSME/private-data/external-storage flow. They are no longer declared by any active listener or `QueueNames` constant, so the admin test should not require them.
- `npm --prefix ./dataland-frontend run checkcypresscompilation` passed via `@command-summarizer` after the queue expectation update.

## Phase 16: Internal Wiki Cleanup

- [x] Update `DatalandInternal.wiki/Onboarding.md` and remove the obsolete EuroDaT setup step from local development onboarding.
- [x] Delete or retire `DatalandInternal.wiki/EuroDaT‐client.md` because the EuroDaT client, dummy EuroDaT services, and external-storage service were removed.
- [x] Update `DatalandInternal.wiki/_Sidebar.md` and remove the EuroDaT Client navigation entry.
- [x] Update `DatalandInternal.wiki/Development-Workflow.md` and remove the obsolete CD option text for ignoring external-storage startup errors.
- [x] Update `DatalandInternal.wiki/Deployment.md` and remove the obsolete deployment checklist entries for ignoring external-storage startup errors.
- [x] Update `DatalandInternal.wiki/Maintenance.md` and remove the stale EuroDaT client maintenance section while preserving unrelated maintenance notes.
- [x] Re-run focused wiki searches for `vsme|VSME`, `EuroDaT|Eurodat|eurodat`, and `external-storage|externalStorage|EXTERNAL_STORAGE|IGNORE_EXTERNAL_STORAGE|CYPRESS_IGNORE_EXTERNAL_STORAGE|INTERNAL_EURODATCLIENT`.
- [x] Confirm no remaining internal wiki references describe removed VSME, EuroDaT, dummy EuroDaT, or external-storage runtime/setup behavior.

## Phase 16 Notes

- Removed local onboarding instructions for EuroDaT credentials, keystore placement, and the EuroDaT client wiki link.
- Deleted the obsolete EuroDaT client page, including dummy EuroDaT and external-storage setup instructions.
- Removed the EuroDaT Client sidebar entry.
- Removed obsolete CD/deployment checkbox instructions for ignoring external-storage startup errors.
- Removed the stale EuroDaT client maintenance heading and renumbered the following maintenance sections.
- Focused searches in `DatalandInternal.wiki` returned no hits for VSME, EuroDaT/eurodat, or external-storage/runtime ignore terms after the cleanup.

## Phase 17: Remove Private Framework Abstraction

- [x] Remove private-framework generation support from `dataland-framework-toolbox`.
- [x] Remove `isPrivateFramework` constructor/property plumbing from `PavedRoadFramework` and `InDevelopmentPavedRoadFramework`.
- [x] Simplify generated backend framework controllers and frontend framework definitions/API clients to public-only templates.
- [x] Delete private framework Freemarker templates.
- [x] Stop generating private framework registry imports.
- [x] Remove frontend private-framework registries, helper functions, and empty `PRIVATE_FRAMEWORKS` constant.
- [x] Remove private document download handling and private/inaccessible dataset request UI.
- [x] Remove community-manager private access-request endpoint, service methods, email builder, email templates, and unused routing key.
- [x] Remove generated `reportingPeriodsOfStoredAccessRequests` response field from single data request responses.
- [x] Regenerate framework outputs, community-manager OpenAPI docs, and downstream clients.
- [x] Run focused verification and final searches.

## Phase 17 Notes

- `dataland-framework-toolbox` now generates only public framework controllers, frontend API clients, framework definitions, and public/all registry imports.
- Deleted frontend private framework registry files and replaced the remaining non-private helpers from `Frameworks.ts` with focused `FrameworkFamilies.ts` and `FrameworkTypes.ts` modules.
- Removed the request-access UI for inaccessible private datasets because all remaining frameworks are public after VSME removal.
- Removed the `DataAccessApi`/`DataAccessController` endpoint and `DataAccessManager`; no active code path still creates private access requests.
- Removed retired access-request email content, templates, and tests.
- `AccessStatus` itself remains because it is still part of persisted data request history and filtering APIs; removing or migrating that model should be a separate persisted-data phase.
- Generation passed via `@command-summarizer`: `./gradlew :dataland-framework-toolbox:runCoverage --args='sfdr'`, `./gradlew :dataland-community-manager:generateOpenApiDocs`, and `./gradlew :dataland-backend:generateClients :dataland-frontend:generateClients :dataland-e2etests:generateClients`.
- Verification passed via `@command-summarizer`: `:dataland-framework-toolbox:test`, `:dataland-backend-utils:compileKotlin`, `:dataland-message-queue-utils:test`, `:dataland-email-service:test`, `:dataland-community-manager:test`, `:dataland-backend:compileKotlin`, `:dataland-e2etests:compileTestKotlin`, frontend `typecheck`, frontend `checkcypresscompilation`, and `testing/verifyOpenApiFiles.sh`.
- Final searches for private-framework and private access-request terms only hit historical notes in this cleanup plan.

## Phase 18: Remove Legacy AccessStatus Data-Request Machinery

- [x] Load `dataland-openapi-impact` and `dataland-frontend-impact` before editing, because this phase changes the community-manager API contract and generated frontend/E2E/backend clients.
- [x] Read package-specific `AGENTS.md` files before editing `dataland-community-manager/`, `dataland-backend-utils/`, `dataland-frontend/`, and `dataland-e2etests/`.
- [x] Run `git status --short` and note unrelated worktree changes without reverting them.
- [x] Reconfirm the current access-status blast radius with focused searches for `AccessStatus|accessStatus|access_status|AccessStatusParameterNonRequired|ACCESS_STATUS_DESCRIPTION` in source, tests, generated specs, and fixtures.

### Phase 18 Scope And Decisions

- [x] Remove `AccessStatus` entirely from active source, API models, UI, tests, generated clients, and generated fixtures.
- [x] Add a new Flyway migration to drop `request_status_history.access_status`. Do not edit historical migration `dataland-community-manager/src/main/kotlin/db/migration/V8__AddAccessStatusToRequestStatusHistory.kt`.
- [x] Preserve historical references in old Flyway migrations and this cleanup plan only. Final source searches should have no active `AccessStatus`, `accessStatus`, or `access_status` references outside generated artifacts that are expected to be refreshed.
- [x] Decide and document requester email visibility after removing access status. The recommended post-removal behavior is: admins can see requester email addresses; company owners and other non-admin users cannot see requester email addresses unless a separate product requirement says otherwise. This preserves the current public-request masking behavior after VSME/private access removal.
- [x] Decide whether the company-owner page `CompanyDataRequestsOverview.vue` should be deleted or replaced by a read-only company request overview. The recommended cleanup is deletion, because its remaining actions are only Grant, Decline, and Revoke access-status patches.

### Phase 18A: Community Manager Kotlin Source

- [x] Delete `dataland-community-manager/src/main/kotlin/org/dataland/datalandcommunitymanager/model/dataRequest/AccessStatus.kt`.
- [x] Remove `accessStatus` from `DataRequestPatch.kt`, including constructor property, KDoc, Swagger description usage, and generated API schema impact.
- [x] Remove `accessStatus` from `StoredDataRequest.kt`.
- [x] Remove `accessStatus` from `StoredDataRequestStatusObject.kt`; keep only request status, creation timestamp, request-status change reason, and answering data ID.
- [x] Remove `accessStatus` from `ExtendedStoredDataRequest.kt` and its entity conversion.
- [x] Remove the derived `DataRequestEntity.accessStatus` getter and remove `accessStatus = accessStatus` from `toStoredDataRequest()`.
- [x] Remove `RequestStatusEntity.accessStatus`, the `AccessStatus` import, `@Enumerated(EnumType.STRING)`, constructor mapping from `StoredDataRequestStatusObject`, and conversion back to `StoredDataRequestStatusObject`.
- [x] Add a new Flyway migration after `V16__MigrateCompanyRolesWithConstraintUpdate.kt`, likely `V17__DropAccessStatusFromRequestStatusHistory.kt`, that executes `ALTER TABLE request_status_history DROP COLUMN access_status`. Verify whether H2/PostgreSQL syntax needs a conditional or plain statement consistent with existing migrations.
- [x] Update `RequestApi.kt`: remove `AccessStatus` import, `AccessStatusParameterNonRequired` import, access-status KDoc lines, and `accessStatus` parameters from `getDataRequests` and `getNumberOfRequests`.
- [x] Update `RequestController.kt`: remove `AccessStatus` import, remove `accessStatus` method parameters, and remove access-status arguments passed to `DataRequestsFilter`.
- [x] Update `DataRequestsFilter.kt`: remove `accessStatus` field, `AccessStatus` import, `shouldFilterByAccessStatus`, and `preparedAccessStatus`.
- [x] Update `TemporaryTables.kt`: keep the latest status join only if `request_status` is still needed, but remove `access_status` from the selected columns and remove the SQL `status_table.access_status IN ...` filter fragment.
- [x] Update `CommunityManagerDataRequestProcessingUtils.kt`: remove `AccessStatus` import, remove the hardcoded `AccessStatus.Public` argument when new requests are stored, and remove the `accessStatus` parameter from `addNewRequestStatusToHistory()`.
- [x] Update `DataRequestUpdateUtils.kt`: remove `newAccessStatus`, compare only request-status changes plus the existing `RequestStatus.NonSourceable` special case, call `addNewRequestStatusToHistory()` without access status, and log request-status patching without access status.
- [x] Update `DataRequestLogger.kt`: remove the `AccessStatus` import and replace `logMessageForPatchingRequestStatusOrAccessStatus` with a request-status-only logger, or reuse an existing request-status log helper if one exists.
- [x] Update `SecurityUtilsService.kt`: remove `dataRequestPatch.accessStatus` from `notPatchingStatusPriorityComment`, remove `pathingOnlyAccessStatus`, and remove the company-owner-only access-status patch authorization branch. After this phase, non-admin company owners should no longer be able to patch a request solely through the removed access-status path.
- [x] Update `DataRequestMasker.kt`: remove `AccessStatus` import and the `it.accessStatus != AccessStatus.Public` condition. Recommended logic: `allowedToSeeEmailAddress = isUserAdmin()` only, unless the product explicitly chooses company-owner email visibility.
- [x] Update `DataRequestRepository.kt` comments/Javadocs that mention `accessStatus` filters.

### Phase 18B: Community Manager Tests

- [x] Update all community-manager tests that import or construct `AccessStatus`, including `DataRequestUpdateManagerTestDataProvider.kt`, `DataRequestMaskerTest.kt`, `DataRequestUpdateUtilsTest.kt`, `DataRequestUpdateManagerTest.kt`, `DataRequestSummaryNotificationServiceTest.kt`, `DataRequestTimeSchedulerTest.kt`, and `DataRequestEntityTest.kt`.
- [x] Delete tests whose only purpose is access-status patching or permission behavior, such as the `DataRequestPatch(accessStatus = ...)` case in `DataRequestUpdateManagerTest.kt`.
- [x] Update `StoredDataRequestStatusObject(...)`, `StoredDataRequest(...)`, and `ExtendedStoredDataRequest(...)` constructor calls after removing `accessStatus`.
- [x] Update data-request masking tests to assert the new requester-email visibility rule from Phase 18 scope decisions.

### Phase 18C: Backend Utils Swagger Cleanup

- [x] Update `dataland-backend-utils/src/main/kotlin/org/dataland/datalandbackendutils/utils/swaggerdocumentation/CommunityManagerOpenApiCustomAnnotations.kt` and remove `AccessStatusParameterNonRequired`.
- [x] Update `dataland-backend-utils/src/main/kotlin/org/dataland/datalandbackendutils/utils/swaggerdocumentation/CommunityManagerOpenApiDescriptionsAndExamples.kt` and remove `ACCESS_STATUS_DESCRIPTION` if no active source still references it.
- [x] Run or include `:dataland-backend-utils:compileKotlin` in verification, because community-manager source imports shared Swagger annotations/descriptions.

### Phase 18D: Frontend Source And Navigation

- [x] Remove or replace `dataland-frontend/src/components/pages/CompanyDataRequestsOverview.vue`. Recommended: delete the page because its only actions are Grant, Decline, and Revoke access-status changes.
- [x] If deleting `CompanyDataRequestsOverview.vue`, update `dataland-frontend/src/router/index.ts`: remove the lazy import and `/companyrequests` route.
- [x] If deleting `CompanyDataRequestsOverview.vue`, update `dataland-frontend/src/components/general/DatasetsTabMenu.vue`: remove the `data-requests-for-my-companies` tab and associated `requestsForMyCompaniesTab` visibility logic.
- [x] Update any component tests that assert the `DATA REQUESTS FOR MY COMPANIES` tab exists or is hidden/visible.
- [x] Update `MyDataRequestsOverviewLegacy.vue`: remove the access-status filter dropdown, selected/available access-status state, access-status column, filtering function, reset handling, and `retrieveAvailableAccessStatuses` import usage.
- [x] Update `AdminAllRequestsOverviewLegacy.vue`: remove the access-status column and any `DatalandTag` usage that only displayed access status.
- [x] Update `ViewDataRequestPageLegacy.vue`: remove the access-status badge/display from the request detail page.
- [x] Update `StatusHistoryLegacy.vue`: remove the access-status column, `accessStatusBadgeClass` import, and `accessStatusEntry` test selectors.
- [x] Update `DatalandTagLegacy.vue`: remove `AccessStatus` import and the five access-status switch cases. Keep request-status and request-priority behavior intact.
- [x] Update `RequestUtilsLegacy.ts`: remove `AccessStatus` import, remove the `accessStatus` parameter from `patchDataRequest()`, stop sending `accessStatus` in the patch body, and delete `accessStatusBadgeClass()`.
- [x] Update `RequestsOverviewPageUtilsLegacy.ts`: remove `AccessStatus` import and `retrieveAvailableAccessStatuses()`.
- [x] Update `ReviewRequestButtonsLegacy.vue`: remove `AccessStatus` import and `accessStatus` parameter plumbing into `patchDataRequest()`.
- [x] Verify modern `MyDataRequestsOverview.vue` and `AdminAllRequestsOverview.vue` remain clean; current analysis found no active access-status usage there.

### Phase 18E: Frontend Tests And Fake Fixture Sources

- [x] Update component tests and mocks that construct access-status values: `AdminAllRequestsOverviewLegacy.cy.ts`, `MyDataRequestsOverviewLegacy.cy.ts`, `ViewDataRequestPageLegacy.cy.ts`, and `StatusHistoryLegacy.cy.ts`.
- [x] Update `dataland-frontend/tests/e2e/fixtures/custom_mocks/StoredDataRequestsFaker.ts`: remove `AccessStatus` import, random access-status generation, `accessStatus` fields, and helper parameters that set access status.
- [x] Do not manually edit generated fixture output `testing/data/DataRequestsMock.json`; regenerate it through the fake-fixture workflow after source/faker changes.
- [x] After regeneration, expected generated fixture output should no longer contain `accessStatus` entries.

### Phase 18F: E2E Kotlin Tests

- [x] Update `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/communityManager/QueryDataRequestsTest.kt`: remove `AccessStatus` import, remove access-status filtering/assertion tests, and remove access-status arguments from `getDataRequests()` calls.
- [x] Update `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/communityManager/QueryDataRequestsCountingTests.kt`: remove access-status count-filter assertions and access-status arguments from `getNumberOfRequests()` calls.
- [x] Update `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/communityManager/CommunityManagerListenerTest.kt`: remove `AccessStatus` import, remove nullable `accessStatus` helper arguments, and update generated `DataRequestPatch(...)` calls after the field is removed.
- [x] Check `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/utils/communityManager/GeneralCommunityManagerTestUils.kt` after client regeneration; it may only need constructor/signature adjustments.

### Phase 18G: OpenAPI And Generated Clients

- [x] Run `./gradlew :dataland-community-manager:generateOpenApiDocs` via `@command-summarizer` after source changes. Confirm `communityManagerOpenApi.json` no longer contains the `AccessStatus` schema, `accessStatus` request parameters, or `accessStatus` model properties.
- [x] Regenerate all known community-manager client consumers via `@command-summarizer`: `./gradlew :dataland-backend:generateClients :dataland-frontend:generateClients :dataland-e2etests:generateClients :dataland-qa-service:generateClients :dataland-user-service:generateClients :dataland-data-sourcing-service:generateClients :dataland-accounting-service:generateClients :dataland-document-manager:generateClients :dataland-batch-manager:generateClients`.
- [x] If a listed module lacks a `generateClients` task in the current branch, document that and run the closest compile check for that module instead.
- [x] Do not hand-edit generated clients or generated OpenAPI JSON. Fix source and rerun generation.

### Phase 18H: Verification

- [x] Run `./gradlew :dataland-community-manager:test` via `@command-summarizer`.
- [x] Run `./gradlew :dataland-backend-utils:compileKotlin` via `@command-summarizer`.
- [x] Run `./gradlew :dataland-e2etests:compileTestKotlin` via `@command-summarizer`.
- [x] Run frontend checks via `@command-summarizer`: `./gradlew :dataland-frontend:npm_run_typecheck`, `./gradlew :dataland-frontend:npm_run_checkcypresscompilation`, and `./gradlew :dataland-frontend:npm_run_checkfakefixturecompilation`.
- [x] Run fake fixture generation or verification via `@command-summarizer`: prefer `testing/verify_that_fake_fixtures_are_up_to_date.sh`; if it reports expected generated diffs, include them and document that no fixture was manually edited.
- [x] Run `testing/verifyOpenApiFiles.sh` via `@command-summarizer`.
- [x] Run targeted compile checks for regenerated community-manager client consumers if generation touched them: at minimum `:dataland-backend:compileKotlin`, `:dataland-qa-service:compileKotlin`, `:dataland-user-service:compileKotlin`, `:dataland-data-sourcing-service:compileKotlin`, `:dataland-accounting-service:compileKotlin`, `:dataland-document-manager:compileKotlin`, and `:dataland-batch-manager:compileKotlin` where those modules/tasks exist.
- [x] Run `./gradlew ktlintFormat` while editing Kotlin, then `./gradlew ktlintCheck detekt` via `@command-summarizer` before finishing if production Kotlin changed.

### Phase 18I: Final Searches And Expected Remaining Hits

- [x] Search active source and tests for `AccessStatus|accessStatus|access_status|AccessStatusParameterNonRequired|ACCESS_STATUS_DESCRIPTION` after regeneration.
- [x] Expected allowed remaining hits: historical Flyway migration `V8__AddAccessStatusToRequestStatusHistory.kt`, the new drop-column migration if its filename/body mentions `access_status`, generated diff notes in this cleanup plan, and possibly git-deleted/generated files before final cleanup. No active API/model/frontend/E2E code should reference access status.
- [x] Search generated OpenAPI/client/fixture outputs for `AccessStatus|accessStatus|access_status` and confirm only intentionally historical or removed-file references remain.
- [x] Update Phase 18 notes after implementation with exact commands run, whether fake fixture verification is green, and any intentionally remaining references.

### Phase 18 Notes

- Removed `AccessStatus` from active community-manager API/domain/source/test code and deleted `AccessStatus.kt`.
- Added `V17__DropAccessStatusFromRequestStatusHistory.kt` to drop `request_status_history.access_status`; historical `V8__AddAccessStatusToRequestStatusHistory.kt` was not edited.
- Requester email visibility is now admin-only after access-status removal; company owners can still query their company requests but requester emails remain masked.
- Deleted `CompanyDataRequestsOverview.vue`, removed the `/companyrequests` route, and removed the “DATA REQUESTS FOR MY COMPANIES” tab.
- Removed access-status columns, filters, badges, patch parameters, and test data from legacy frontend request pages, Cypress component tests, and fake fixture source.
- Removed access-status query/count/patch usage from Kotlin E2E tests.
- Regeneration passed via `@command-summarizer`: `./gradlew :dataland-community-manager:generateOpenApiDocs` and `./gradlew :dataland-backend:generateClients :dataland-frontend:generateClients :dataland-e2etests:generateClients :dataland-qa-service:generateClients :dataland-user-service:generateClients :dataland-data-sourcing-service:generateClients :dataland-accounting-service:generateClients :dataland-document-manager:generateClients :dataland-batch-manager:generateClients`.
- Fake fixture generation completed through `testing/verify_that_fake_fixtures_are_up_to_date.sh`, but the script failed its final diff check because generated fixture outputs changed as expected: `testing/data/DataRequestsMock.json`, `testing/data/EuTaxonomyNonFinancialsQaReportPreparedFixtures.json`, `testing/data/SfdrLinkedDataAndQaReportPreparedFixtures.json`, and `testing/data/SfdrQaReportPreparedFixtures.json`. No generated fixture output was manually edited.
- Verification passed via `@command-summarizer`: `:dataland-community-manager:test`, `:dataland-backend-utils:compileKotlin`, `:dataland-e2etests:compileTestKotlin`, frontend `typecheck`, frontend `checkcypresscompilation`, frontend `checkfakefixturecompilation`, `testing/verifyOpenApiFiles.sh`, regenerated-client consumer compiles for backend/QA/user/data-sourcing/accounting/document-manager/batch-manager, and `./gradlew ktlintCheck detekt`.
- A combined `./gradlew ktlintFormat ktlintCheck detekt` run hit Gradle implicit-dependency validation before detekt because ktlint format tasks consumed generated backend-client outputs without declared dependencies. The CI-style `./gradlew ktlintCheck detekt` rerun passed.
- Final searches for `AccessStatus|accessStatus|access_status|AccessStatusParameterNonRequired|ACCESS_STATUS_DESCRIPTION` are clean in active backend-utils, frontend, E2E, generated OpenAPI, and generated fixture outputs. Remaining community-manager hits are only the historical V8 migration and the new V17 drop-column migration.

## Phase 19: Legacy VSME Persisted-Data Cleanup Decision

- [ ] Decide whether legacy persisted `vsme` data should be removed by a minimal backend-only migration or by a broader full purge across service-local databases.
- [ ] Backend-only option: add a backend Flyway migration that deletes `data_point_uuid_map` rows for VSME dataset IDs, deletes matching `dataset_datapoint` rows, deletes `data_meta_information` rows where `data_type = 'vsme'`, and deletes `non_sourceability_information` rows where `data_type = 'vsme'` if such rows exist. This directly fixes metadata/search 500s caused by `DataType.valueOf("vsme")` after VSME is removed from the active registry, with the smallest production data blast radius.
- [ ] Full-purge option: add coordinated service-local migrations that also remove VSME payload/workflow rows from `dataland-internal-storage`, `dataland-qa-service`, `dataland-community-manager`, and `dataland-data-sourcing-service`. This would delete historical QA, request, and sourcing state in addition to backend metadata, so it needs explicit product/data-retention approval.
- [ ] Do not delete `data_point_meta_information` or `data_point_items` just because they are linked to VSME datasets; datapoints can be reusable assembled-data components, and ownership cannot be inferred safely without an audited dataset-to-datapoint retention decision.
- [ ] Do not delete document-manager files or metadata as part of this cleanup unless a separate document-reference audit proves the documents are owned only by retired VSME datasets.
- [ ] If the backend-only option is chosen, add a focused migration test that seeds one VSME and one non-VSME dataset with composition rows, then verifies only the VSME metadata and composition rows are removed.
- [ ] If the full-purge option is chosen, define exact per-service delete ordering before implementation: community-manager child rows before `data_requests`, data-sourcing `requests` before `data_sourcing`, QA child/collection rows before `dataset_judgement`, and internal-storage payload deletion by confirmed VSME JSON marker rather than by guessed IDs.
