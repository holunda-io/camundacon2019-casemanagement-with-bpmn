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
import org.camunda.bpm.engine.ProcessEngineServices
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

  private fun CaseProcessInstanceWrapper.findTask(key:String) : Task? = camunda.taskService.createTaskQuery().processInstanceBusinessKey(businessKey).taskDefinitionKey(key).singleResult()

  @Test
  fun `manually start and stop case task`() {
    val processInstance = startProcess()

    assertThat(processInstance.findTask(DummyCaseProcess.Elements.USERTASK_MS_REPETITION_COMPLETE.key)).isNull()

    val started = processInstance.startManually(manualStart_repetitionComplete.key)

    val task = processInstance.findTask(DummyCaseProcess.Elements.USERTASK_MS_REPETITION_COMPLETE.key)
    assertThat(task).isNotNull()

    val entity = processInstance.repository.findById(started.get())!!

    assertThat(entity.executionId).isNotNull()
    assertThat(entity.state).isEqualTo(BpmnCaseExecutionState.ACTIVE)

    camunda.taskService.complete(task!!.id)

    val completed = processInstance.repository.findById(started.get())!!
    assertThat(completed.state).isEqualTo(BpmnCaseExecutionState.COMPLETED)
  }

  @Test
  fun `a manually started caseExecution does not pollute the global variable scope`() {
    val processInstance = startProcess()
    fun hasCaseExecutionVariable() = camunda.runtimeService.getVariables(processInstance.processInstanceId).keys.contains(CaseProcess.VARIABLES.caseExecutionId)
    assertThat(hasCaseExecutionVariable()).isFalse()

    processInstance.startManually(manualStart_repetitionComplete.key)

    // variable is not readable from global scope
    assertThat(hasCaseExecutionVariable()).isFalse()
  }

  /**
   * see issue #6
   */
  @Test
  fun `can not start a caseExecution with repetition rule completed until the predecessor is finished`() {
    val processInstance = startProcess()
    assertThat(processInstance.startManually(manualStart_repetitionComplete.key)).isNotEmpty

    // second start does not create a new instance
    assertThat(processInstance.startManually(manualStart_repetitionComplete.key)).isEmpty

    // complete the task
    BpmnAwareTests.complete(processInstance.findTask(DummyCaseProcess.Elements.USERTASK_MS_REPETITION_COMPLETE.key))

    // now we can start again
    assertThat(processInstance.startManually(manualStart_repetitionComplete.key)).isNotEmpty
  }

  @Test
  fun `can start a caseExecution with repetition rule manualStart anytime`() {
    val processInstance = startProcess()
    assertThat(processInstance.startManually(DummyCaseProcess.CaseTasks.manualStart_repetitionManualStart.key)).isNotEmpty
    assertThat(processInstance.startManually(DummyCaseProcess.CaseTasks.manualStart_repetitionManualStart.key)).isNotEmpty
  }

  private fun startProcess(): DummyCaseProcessInstance {
    val processInstance = process.start(Start())

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("keep_alive")

    return processInstance
  }
}
