package io.holunda.extension.casemanagement._test.stage

import _test.DummyCaseProcess
import _test.DummyCaseProcessInstance
import cmmn.BpmnCaseExecutionState
import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import com.tngtech.jgiven.annotation.Quoted
import io.holunda.extension.casemanagement.ActivityId
import io.holunda.extension.casemanagement._test.JGivenKotlinStage
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat as bpmnAssertThat

@JGivenKotlinStage
class DummyCaseProcessThenStage : Stage<DummyCaseProcessThenStage>() {

  @ExpectedScenarioState
  lateinit var camunda: ProcessEngineRule

  @ExpectedScenarioState
  lateinit var processInstance: DummyCaseProcessInstance

  @ExpectedScenarioState
  lateinit var repositoryFactory: BpmCaseExecutionRepositoryFactory

  fun `the process is waiting at $`(activityId: ActivityId): DummyCaseProcessThenStage {
    bpmnAssertThat(processInstance).isWaitingAt(activityId.key)

    return self()
  }

  fun `all pending executions for task $ have state $`(
    @Quoted caseTask: DummyCaseProcess.CaseTask,
    @Quoted expectedState: BpmnCaseExecutionState) = self().apply {

    val executions = repositoryFactory.create(camunda.runtimeService, processInstance.processInstanceId).findByCaseTaskKey(caseTask.key)

    assertThat(executions.map { it.state }.filter { it != BpmnCaseExecutionState.ACTIVE && it != BpmnCaseExecutionState.COMPLETED }.toSet())
      .containsOnly(expectedState)
  }
}
