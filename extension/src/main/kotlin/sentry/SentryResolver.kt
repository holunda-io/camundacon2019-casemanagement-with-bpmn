package io.holunda.extension.casemanagement.sentry

import io.holunda.extension.casemanagement.bpmn.CaseTaskDefinition
import io.holunda.extension.casemanagement.expressionManager
import org.camunda.bpm.engine.delegate.DelegateExecution

/**
 * Executes the expression configured on caseTaskDefinition (if present).
 */
fun evaluateSentryCondition(execution: DelegateExecution, caseTaskDefinition: CaseTaskDefinition): Boolean =
  if (caseTaskDefinition.hasSentry)
    execution.expressionManager
      .createExpression(caseTaskDefinition.sentryOnPartExpression)
      .getValue(execution, null) as Boolean
  else true

