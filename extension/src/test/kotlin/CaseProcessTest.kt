package io.holunda.extension.casemanagement

import _test.DummyCaseProcess
import _test.DummyCaseProcess.CaseTasks.manualStart_repetitionComplete
import _test.DummyCaseProcess.CaseTasks.runAutomatically_repetitionNone
import _test.DummyCaseProcessInstance
import _test.Start
import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.extension.mockito.CamundaMockito
import org.junit.Rule
import org.junit.Test

@Deployment(resources = [DummyCaseProcess.BPMN])
class CaseProcessTest {

  private val om = jacksonObjectMapper()

  @get:Rule
  val camunda = ProcessEngineRule(CamundaTestConfiguration().buildProcessEngine()).apply {
    BpmnAwareTests.init(this.processEngine)
  }

  val process: DummyCaseProcess by lazy {
    DummyCaseProcess(camunda.runtimeService, camunda.repositoryService).apply {
      CamundaMockito.registerInstance(this)
    }
  }

  val repositoryFactory = BpmCaseExecutionRepositoryFactory(om)

  @Test
  fun `start process`() {
    val processInstance = startProcess()

    val def = processInstance.caseProcessDefinition
    assertThat(def.tasks[runAutomatically_repetitionNone.key]!!.repetitionRule).isEqualTo(RepetitionRule.NONE)

    assertThat(processInstance.bpmnCaseExecutionEntities.executions).isNotEmpty()

    val enabled = processInstance.findExecutions(state = BpmnCaseExecutionState.ENABLED)

    //val repository = repositoryFactory.create(camunda.runtimeService, processInstance.id)


  }

  @Test
  fun `manually start and stop case task`() {
    val processInstance = startProcess()

    // task does not exist
    val userTask = "ut_manualStart_repetitionComplete"
    fun queryTask(task: String): Task? = camunda.taskService.createTaskQuery().processInstanceBusinessKey(processInstance.businessKey).taskDefinitionKey(task).singleResult()
    assertThat(queryTask(userTask)).isNull()

    val started = processInstance.startManually(manualStart_repetitionComplete.key)

    val task = queryTask(userTask)
    assertThat(task).isNotNull

    val entity = processInstance.repository.findById(started.get())!!

    assertThat(entity.executionId).isNotNull()
    assertThat(entity.state).isEqualTo(BpmnCaseExecutionState.ACTIVE)

    camunda.taskService.complete(task!!.id)

    val completed = processInstance.repository.findById(started.get())!!
    assertThat(completed.state).isEqualTo(BpmnCaseExecutionState.COMPLETED)
  }

  private fun startProcess(): DummyCaseProcessInstance {
    val processInstance = process.start(Start())

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("keep_alive")

    return processInstance
  }
}


