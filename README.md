# camunda-case-bpmn

Modelling and Managing a Case with BPMN/DMN



## State Chain

- just created -> NEW
- No Sentry and no manualStart -> ACTIVE
- No Sentry and manualStart -> ENABLED
- Sentry(true) and manualStart -> ENABLED
- Sentry(false) and manualStart -> DISABLED
- ENABLED on manualStart -> ACTIVE
- ACTIVE on complete -> COMPLETED
