package io.holunda.extension.casemanagement._test.stage

import _test.DummyCaseProcess.CaseTask
import _test.DummyCaseProcessInstance
import cmmn.BpmnCaseExecutionState
import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import com.tngtech.jgiven.annotation.Quoted
import io.holunda.extension.casemanagement.ActivityId
import io.holunda.extension.casemanagement._test.JGivenKotlinStage
import io.holunda.extension.casemanagement._test.step
import io.holunda.extension.casemanagement.findTask
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.ProcessEngineRule
import java.util.*
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat as bpmnAssertThat

@JGivenKotlinStage
@Suppress("UNUSED")
class DummyCaseProcessThenStage : Stage<DummyCaseProcessThenStage>() {

  @ExpectedScenarioState
  lateinit var camunda: ProcessEngineRule

  @ExpectedScenarioState
  lateinit var processInstance: DummyCaseProcessInstance

  @ExpectedScenarioState
  lateinit var repositoryFactory: BpmCaseExecutionRepositoryFactory

  @ExpectedScenarioState
  lateinit var startedTasks : MutableMap<CaseTask, MutableList<Optional<String>>>


  fun `the process is waiting at $`(activityId: ActivityId)= step {
    bpmnAssertThat(processInstance).isWaitingAt(activityId.key)
  }

  fun `all pending executions for task $ have state $`(
    @Quoted caseTask: CaseTask,
    @Quoted expectedState: BpmnCaseExecutionState) = step {

    val executions = repositoryFactory.create(camunda.runtimeService, processInstance.processInstanceId).findByCaseTaskKey(caseTask.key)

    assertThat(executions.map { it.state }.filter { it != BpmnCaseExecutionState.ACTIVE && it != BpmnCaseExecutionState.COMPLETED }.toSet())
      .containsOnly(expectedState)
  }

  fun `a user task $ exists`(@Quoted task: ActivityId) = step {
    assertThat(processInstance.findTask(task.key, camunda.taskService)).isNotNull()
  }

  fun `no user task $ exists`(@Quoted task: ActivityId) = step {
    assertThat(processInstance.findTask(task.key, camunda.taskService)).isNull()
  }

  fun `the last started task of $ has state $`(@Quoted caseTask: CaseTask, @Quoted state: BpmnCaseExecutionState) = step {
    val started = startedTasks[caseTask]!!.last().orElseThrow { IllegalStateException("no last execution for task $caseTask found") }
    val entity = processInstance.repository.findById(started)!!

    assertThat(entity.executionId).isNotNull()
    assertThat(entity.state).isEqualTo(state)

  }

  fun `no new caseTask $ exists`(@Quoted caseTask:CaseTask) = step {
    val started = startedTasks[caseTask]!!.last()

    assertThat(started).isEmpty()
  }

  fun `the process does not have a variable $`(@Quoted variableKey:String) = step {
    assertThat(camunda.runtimeService
      .getVariables(processInstance.processInstanceId)
      .keys.contains(variableKey)
    ).isFalse()
  }

}
