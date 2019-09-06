package io.holunda.extension.casemanagement

import _test.DummyCaseProcess
import _test.DummyCaseProcess.CaseTask
import cmmn.BpmnCaseExecutionState
import io.holunda.extension.casemanagement._test.AND
import io.holunda.extension.casemanagement._test.GIVEN
import io.holunda.extension.casemanagement._test.THEN
import org.junit.Test

/**
 * Test that ensures correct behavior of case tasks when a sentry expression is defined.
 */
class SentryStateTest : AbstractDummyCaseProcessTest() {

  @Test
  fun `caseTaskWithSentry is DISABLED after start when sentryCondition=false`() {
    GIVEN
      .`the sentry for task $ evaluates to $`(CaseTask.manualStart_repetitionComplete_withSentry, false)
      .AND
      .`the case process is started`()

    THEN
      .`all pending executions for task $ have state $`(CaseTask.manualStart_repetitionComplete_withSentry, BpmnCaseExecutionState.DISABLED)
  }

  @Test
  fun `a started process waits at keep alive`() {
    GIVEN
      .`the case process is started`()

    THEN
      .`the process is waiting at $`(DummyCaseProcess.ProcessTask.keep_alive)
  }
}
