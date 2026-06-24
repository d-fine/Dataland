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
