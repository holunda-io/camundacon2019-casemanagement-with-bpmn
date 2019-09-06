package io.holunda.extension.casemanagement.sentry

import io.holunda.extension.casemanagement.bpmn.CaseTaskDefinition
import io.holunda.extension.casemanagement.expressionManager
import org.camunda.bpm.engine.delegate.DelegateExecution

fun evaluateSentryCondition(execution: DelegateExecution, caseTaskDefinition: CaseTaskDefinition): Boolean = when (caseTaskDefinition.hasSentry) {
  true -> execution.expressionManager
    .createExpression(caseTaskDefinition.sentryOnPartExpression)
    .getValue(execution, null) as Boolean
  false -> true
}
