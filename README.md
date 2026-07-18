# cloud-itonami-isco-7516

Open Occupation Blueprint for **ISCO-08 7516**: Tobacco Preparers and Tobacco Products Makers.

This repository designs a forkable OSS business for a tobacco-manufacturing factory-floor scheduling and logistics coordination practice: a factory-floor scheduling and supply-coordination robot manages crew/task records under a governor-gated actor, so a tobacco preparation and tobacco-products manufacturing crew keeps its own operating records instead of renting a closed workforce-management SaaS.

**Maturity: `:implemented`.** `src/tobaccocoord/` implements the
`TobaccoCoordActor` as a `langgraph.graph/state-graph`
(`tobaccocoord.actor`) wired to a `Tobacco Preparation Coordination
Advisor` (`tobaccocoord.advisor`) and an independent
`TobaccoCoordGovernor` (`tobaccocoord.governor`), following the
itonami actor pattern (ADR-2607121000): `:intake -> :advise -> :govern
-> :decide -+-> :commit (:ok?) +-> :request-approval (:escalate?,
human-in-the-loop interrupt) +-> :hold (:hard?)`. HARD invariants
(always hold, never overridable): preparer provenance, facility
provenance, no-actuation (`:effect` must be `:propose`), a closed
op-allowlist (`:log-work-record`, `:schedule-crew-operation`,
`:flag-compliance-concern`, `:coordinate-supply-order` — nothing else
may ever be proposed), and a permanent, unconditional block on any
proposal that would directly finalize a manufacturing-execution
decision (e.g. deciding to proceed with a specific production run) or
a regulatory-compliance-clearance decision (e.g. affixing a tax stamp
or finalizing a labeling-compliance determination), or override a shop
safety officer's judgment. Always-escalate paths (human sign-off
regardless of confidence, mapping this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)):
`:flag-compliance-concern` (always) and `:coordinate-supply-order`
above the registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a factory-floor scheduling/logistics coordination robot performs crew scheduling, batch/inventory/progress-record logging and tobacco-materials supply-order coordination for a tobacco preparation and tobacco-products manufacturing crew, under an actor that proposes actions and an independent **Tobacco Preparation Coordination Governor** that gates them. The governor never
dispatches hardware itself, never performs tobacco-preparation or tobacco-product-making work on the factory floor, and never finalizes a manufacturing-execution decision or a regulatory-compliance-clearance decision (tax-stamp affixing, labeling-compliance determination), or overrides a shop safety officer's judgment; `:high`/`:safety-critical` actions (such as a flagged regulatory-compliance/equipment-hazard/dust-exposure concern, or an above-threshold supply order) require human sign-off. **This actor coordinates factory-floor scheduling/logistics only — it never performs tobacco-preparation or tobacco-product-making work itself and never makes regulatory-compliance-clearance decisions itself.**

## Core Contract

```text
crew roster + facility registration + compliance-reporting policy
        |
        v
Tobacco Preparation Coordination Advisor -> TobaccoCoordGovernor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, finalize
a manufacturing-execution decision, finalize a regulatory-compliance-clearance
decision, override a shop safety officer's judgment, suppress an operating
record, or disclose sensitive data without governor approval and audit
evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `7516`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
