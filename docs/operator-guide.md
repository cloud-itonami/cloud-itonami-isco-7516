# Operator Guide

## First Deployment

1. Define the operator's facility coverage and crew intake process.
2. Define consent and purpose categories for preparer/facility records.
3. Run synthetic operating cases (work-log entry, crew-operation
   scheduling, supply coordination, compliance-concern flagging).
4. Enable human-reviewed sign-off for `:high`/`:safety-critical`
   actions (all flagged compliance concerns, above-threshold supply
   orders).
5. Measure operating outcomes and audit coverage.

## Minimum Production Controls

- consent and disclosure log
- regulatory-compliance-and-safety-critical escalation path (equipment
  hazard, dust exposure, tax-stamp/labeling compliance concerns)
- provenance for all operating records (preparer and facility both
  independently registered)
- human review for high-risk cases
- audit export for all gated actions
- a hard, unconditional block on any attempt to route a
  manufacturing-execution decision, a regulatory-compliance-clearance
  decision (tax-stamp affixing, labeling-compliance determination), or
  a shop-safety-officer-override decision, through this actor — those
  decisions stay the shop safety officer's / regulatory-compliance
  function's exclusive authority end to end

## Certification

Certified operators must prove that the governor gates every
safety-critical robot action, that safety-critical and
regulatory-compliance risks escalate to humans, and that no deployment
configuration can route a manufacturing-execution decision, a
regulatory-compliance-clearance decision, or a
shop-safety-officer-judgment override through this actor.
