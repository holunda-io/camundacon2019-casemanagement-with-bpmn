package io.holunda.extension.casemanagement

import _test.DummyCaseProcess.CaseTask
import _test.DummyCaseProcess.CaseTask.*
import _test.DummyCaseProcess.Elements.USERTASK_MS_REPETITION_COMPLETE
import cmmn.BpmnCaseExecutionState
import io.holunda.extension.casemanagement._test.AND
import io.holunda.extension.casemanagement._test.GIVEN
import io.holunda.extension.casemanagement._test.THEN
import io.holunda.extension.casemanagement._test.WHEN
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test

class CaseProcessTest : AbstractDummyCaseProcessTest() {

  @Test
  @Ignore("does not work due to sentry condition expression, refactor test to use jgiven stages")
  fun `start process`() {
    val processInstance = startProcess()

    val def = processInstance.caseProcessDefinition
    assertThat(def.tasks[runAutomatically_repetitionNone.key]!!.repetitionRule).isEqualTo(RepetitionRule.NONE)

    assertThat(processInstance.bpmnCaseExecutionEntities.executions).isNotEmpty()

    val enabled = processInstance.findExecutions(state = BpmnCaseExecutionState.ENABLED)
      .map { it.caseTaskKey }
      .map { CaseTask.valueOf(it) }

    assertThat(enabled).containsExactlyInAnyOrder(
      CaseTask.manualStart_repetitionManualStart,
      manualStart_repetitionComplete,
      manualStart_repetitionNone
    )

  }

  @Test
  fun `manually start and stop case task`() {
    GIVEN
      .`the case process is started`()

    THEN
      .`no user task $ exists`(USERTASK_MS_REPETITION_COMPLETE)

    WHEN
      .`a caseTask $ is manually started`(manualStart_repetitionComplete)

    THEN
      .`a user task $ exists`(USERTASK_MS_REPETITION_COMPLETE)
      .AND
      .`the last started task of $ has state $`(manualStart_repetitionComplete, BpmnCaseExecutionState.ACTIVE)

    WHEN
      .`the userTask $ is completed`(USERTASK_MS_REPETITION_COMPLETE)

    THEN
      .`the last started task of $ has state $`(manualStart_repetitionComplete, BpmnCaseExecutionState.COMPLETED)

  }

  @Test
  fun `a manually started caseExecution does not pollute the global variable scope`() {
    GIVEN
      .`the case process is started`()

    THEN
      .`the process does not have a variable $`(CaseProcess.VARIABLES.caseExecutionId)

    WHEN
      .`a caseTask $ is manually started`(manualStart_repetitionComplete)

    THEN
      .`the process does not have a variable $`(CaseProcess.VARIABLES.caseExecutionId)
  }

  /**
   * see issue #6
   */
  @Test
  fun `can not start a caseExecution with repetition rule completed until the predecessor is finished`() {
    GIVEN
      .`the case process is started`()
      .AND
      .`a caseTask $ is manually started`(CaseTask.manualStart_repetitionComplete)

    THEN
      .`the last started task of $ has state $`(manualStart_repetitionComplete, BpmnCaseExecutionState.ACTIVE)

    // this does not work due to repetition rule
    WHEN
      .`a caseTask $ is manually started`(CaseTask.manualStart_repetitionComplete)

    THEN
      .`no user task $ exists`(manualStart_repetitionComplete)

    WHEN
      .`the userTask $ is completed`(USERTASK_MS_REPETITION_COMPLETE)
      .AND
      .`a caseTask $ is manually started`(manualStart_repetitionComplete)

    THEN
      .`the last started task of $ has state $`(manualStart_repetitionComplete, BpmnCaseExecutionState.ACTIVE)
  }

  @Test
  fun `can start a caseExecution with repetition rule manualStart anytime`() {
    GIVEN
      .`the case process is started`()

    WHEN
      .`a caseTask $ is manually started`(manualStart_repetitionManualStart)

    THEN
      .`the last started task of $ has state $`(manualStart_repetitionManualStart, BpmnCaseExecutionState.ACTIVE)

    WHEN
      .`a caseTask $ is manually started`(manualStart_repetitionManualStart)

    THEN
      .`the last started task of $ has state $`(manualStart_repetitionManualStart, BpmnCaseExecutionState.ACTIVE)

  }

  @Test
  fun `can not start a caseExecution again without repetition rule`() {
    GIVEN
      .`the case process is started`()

    WHEN
      .`a caseTask $ is manually started`(manualStart_repetitionNone)

    THEN
      .`the last started task of $ has state $`(manualStart_repetitionNone, BpmnCaseExecutionState.ACTIVE)

    WHEN
      .`a caseTask $ is manually started`(manualStart_repetitionNone)

    THEN
      .`no new caseTask $ exists`(manualStart_repetitionNone)

  }

}
