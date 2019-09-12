package io.holunda.extension.casemanagement

import _test.DummyCaseProcessBean.CaseTask
import _test.DummyCaseProcessBean.CaseTask.*
import _test.DummyCaseProcessBean.Elements.USERTASK_MS_REPETITION_COMPLETE
import cmmn.BpmnCaseExecutionState
import io.holunda.extension.casemanagement._test.AND
import io.holunda.extension.casemanagement._test.GIVEN
import io.holunda.extension.casemanagement._test.THEN
import io.holunda.extension.casemanagement._test.WHEN
import org.junit.Test

class CaseProcessBeanTest : AbstractDummyCaseProcessTest() {

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
      .`the process does not have a variable $`(CaseProcessBean.VARIABLES.caseExecutionId)

    WHEN
      .`a caseTask $ is manually started`(manualStart_repetitionComplete)

    THEN
      .`the process does not have a variable $`(CaseProcessBean.VARIABLES.caseExecutionId)
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
