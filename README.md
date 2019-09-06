# camunda-case-bpmn

Modelling and Managing a Case with BPMN/DMN

## Features




### State Chain

- just created -> NEW
- No Sentry and no manualStart -> ACTIVE
- No Sentry and manualStart -> ENABLED
- Sentry(true) and manualStart -> ENABLED
- Sentry(false) and manualStart -> DISABLED
- ENABLED on manualStart -> ACTIVE
- ACTIVE on complete -> COMPLETED

## Roadmap

soon:

* automatic start of case tasks

near future:

* modeler template: to set extension properties with key/value support
* modeler plugin: display overlay icons for mandatory, manualStart and repetitionRule
* cockpit plugin: display cast task states for running and waiting instances