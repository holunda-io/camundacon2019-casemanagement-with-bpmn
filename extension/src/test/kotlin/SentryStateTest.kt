package io.holunda.extension.casemanagement

import _test.DummyCaseProcess
import _test.DummyCaseProcess.CaseTask
import _test.DummyCaseProcess.CaseTask.manualStart_repetitionComplete_withSentry
import cmmn.BpmnCaseExecutionState
import io.holunda.extension.casemanagement._test.AND
import io.holunda.extension.casemanagement._test.GIVEN
import io.holunda.extension.casemanagement._test.THEN
import io.holunda.extension.casemanagement._test.WHEN
import org.junit.Test

/**
 * Test that ensures correct behavior of case tasks when a sentry expression is defined.
 */
class SentryStateTest : AbstractDummyCaseProcessTest() {

  @Test
  fun `caseTaskWithSentry is DISABLED after start when sentryCondition=false`() {
    GIVEN
      .`the sentry for task $ evaluates to $`(manualStart_repetitionComplete_withSentry, false)
      .AND
      .`the case process is started`()

    THEN
      .`all pending executions for task $ have state $`(manualStart_repetitionComplete_withSentry, BpmnCaseExecutionState.DISABLED)
  }

  @Test
  fun `caseTaskWithSentry is ENABLED after start when sentryCondition=true`() {
    GIVEN
      .`the sentry for task $ evaluates to $`(manualStart_repetitionComplete_withSentry, true)
      .AND
      .`the case process is started`()

    THEN
      .`all pending executions for task $ have state $`(manualStart_repetitionComplete_withSentry, BpmnCaseExecutionState.ENABLED)
  }



  @Test
  fun `the follow up task for repetitionRule "complete" is disable`() {
    GIVEN
      .`the sentry for task $ evaluates to $`(manualStart_repetitionComplete_withSentry, true)
      .AND
      .`the case process is started`()

    WHEN
      .`a caseTask $ is manually started`(manualStart_repetitionComplete_withSentry)
      .AND
      // the sentry evaluation changes
      .`the sentry for task $ evaluates to $`(manualStart_repetitionComplete_withSentry, false)
      .AND
      .`the userTask $ is completed`(DummyCaseProcess.Elements.USERTASK_MS_REPETITION_COMPLETE_WITH_SENTRY)

    THEN
      .`all pending executions for task $ have state $`(manualStart_repetitionComplete_withSentry, BpmnCaseExecutionState.DISABLED)
  }

  @Test
  fun `a disabled task is enabled when the sentry changes`() {
    GIVEN
      .`the sentry for task $ evaluates to $`(manualStart_repetitionComplete_withSentry, false)
      .AND
      .`the case process is started`()

    THEN
      .`all pending executions for task $ have state $`(manualStart_repetitionComplete_withSentry, BpmnCaseExecutionState.DISABLED)

    WHEN
      .`the sentry for task $ evaluates to $`(manualStart_repetitionComplete_withSentry, true)
      .AND
      .`sentry re-evaluation is triggered`()

    THEN
      .`all pending executions for task $ have state $`(manualStart_repetitionComplete_withSentry, BpmnCaseExecutionState.ENABLED)
  }

  @Test
  fun `an enabled task is disabled when the sentry changes`() {
    GIVEN
      .`the sentry for task $ evaluates to $`(manualStart_repetitionComplete_withSentry, true)
      .AND
      .`the case process is started`()

    THEN
      .`all pending executions for task $ have state $`(manualStart_repetitionComplete_withSentry, BpmnCaseExecutionState.ENABLED)

    WHEN
      .`the sentry for task $ evaluates to $`(manualStart_repetitionComplete_withSentry, false)
      .AND
      .`sentry re-evaluation is triggered`()

    THEN
      .`all pending executions for task $ have state $`(manualStart_repetitionComplete_withSentry, BpmnCaseExecutionState.DISABLED)
  }

  // TODO move to general process test
  @Test
  fun `a started process waits at keep alive`() {
    GIVEN
      .`the case process is started`()

    THEN
      .`the process is waiting at $`(DummyCaseProcess.ProcessTask.keep_alive)
  }
}
