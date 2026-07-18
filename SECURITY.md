# Security Policy

This project handles tobacco preparers and tobacco products makers operating
workflows. Treat vulnerabilities as potentially high impact even when the
demo data is synthetic — this domain's failure modes include physical
worker-safety risk from equipment exposure and dust/particulate hazards, as
well as regulatory-compliance risk (tax-stamp and labeling compliance) for
the manufacturing process itself.

## Do Not Disclose Publicly

Report privately before opening public issues for:

- credential exposure
- real preparer, facility or operator data exposure
- authorization bypass
- TobaccoCoordGovernor bypass
- audit-ledger tampering
- over-disclosure in reports or exports
- unsafe robot action dispatch
- any path that lets a proposal reach a manufacturing-execution
  decision, a regulatory-compliance-clearance decision (tax-stamp
  affixing, labeling-compliance determination), or a
  shop-safety-officer-judgment-override decision

## Reporting

Use GitHub private vulnerability reporting when available for the repository.
If that is unavailable, contact the repository maintainers through the
cloud-itonami organization before publishing details.

Include:

- affected commit or version
- reproduction steps
- expected and actual behavior
- impact on preparer/facility data, policy enforcement or audit logging
- suggested fix, if known

## Production Guidance

- Store secrets outside Git.
- Keep real preparer/facility/operator data outside this repository.
- Run policy tests before deployment.
- Export and review audit logs regularly.
- Use least privilege for operators and service accounts.
