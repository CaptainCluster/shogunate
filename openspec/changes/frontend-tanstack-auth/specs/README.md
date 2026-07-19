# Specification Delta

No requirement changes for this change.

Implements existing frontend architecture constraints in `docs/ARCHITECTURE.md` §7.2 and `openspec/AGENTS.md`. User-visible auth behavior in `openspec/specs/user-auth/spec.md` is unchanged.

The archived `user-auth` change used React Context (`AuthProvider`) for session state; that approach is superseded by this change.
