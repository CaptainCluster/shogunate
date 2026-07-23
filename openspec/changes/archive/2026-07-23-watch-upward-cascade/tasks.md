## 1. Backend verification

- [x] 1.1 Confirm `WatchHierarchySyncService` promotes season/show when all episodes watched with max episode `watchedAt`
- [x] 1.2 Confirm upward demotion clears season/show when an episode is unmarked and hierarchy is incomplete
- [x] 1.3 Confirm upward events use `triggeredByCascade=true` referencing the initiating user action event
- [x] 1.4 Run `./gradlew test --tests "com.tvtracker.watch.*"` and fix any gaps against delta spec scenarios

## 2. Documentation

- [x] 2.1 Update `docs/ARCHITECTURE.md` §5: document bidirectional cascade, `WatchHierarchySyncService`, and transaction order
- [x] 2.2 Update `docs/PRD.md` §5.3: add upward cascade bullets and parent timestamp rule

## 3. Close out

- [x] 3.1 Run `./gradlew spotlessApply check` and resolve any lint issues
- [x] 3.2 Archive change with `/opsx:archive` to merge delta into `openspec/specs/watch/spec.md`
