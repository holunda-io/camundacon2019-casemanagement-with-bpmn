package io.holunda.extension.casemanagement

import _test.DummyCaseProcess
import _test.DummyCaseProcess.CaseTask
import _test.DummyCaseProcess.CaseTask.*
import _test.DummyCaseProcess.Elements.*
import _test.DummyCaseProcessInstance
import _test.Start
import cmmn.BpmnCaseExecutionState
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
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

  private fun CaseProcessInstanceWrapper.findTask(key:String) : Task? = camunda.taskService.createTaskQuery().processInstanceBusinessKey(businessKey).taskDefinitionKey(key).singleResult()

  @Test
  fun `manually start and stop case task`() {
    val processInstance = startProcess()

    assertThat(processInstance.findTask(USERTASK_MS_REPETITION_COMPLETE.key)).isNull()

    val started = processInstance.startWithRepetitionComplete()

    val task = processInstance.findTask(USERTASK_MS_REPETITION_COMPLETE.key)
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

    processInstance.startWithRepetitionComplete()

    // variable is not readable from global scope
    assertThat(hasCaseExecutionVariable()).isFalse()
  }

  /**
   * see issue #6
   */
  @Test
  fun `can not start a caseExecution with repetition rule completed until the predecessor is finished`() {
    val processInstance = startProcess()
    assertThat(processInstance.startWithRepetitionComplete()).isNotEmpty

    // second start does not create a new instance
    assertThat(processInstance.startWithRepetitionComplete()).isEmpty

    // complete the task
    BpmnAwareTests.complete(processInstance.findTask(DummyCaseProcess.Elements.USERTASK_MS_REPETITION_COMPLETE.key))

    // now we can start again
    assertThat(processInstance.startManually(manualStart_repetitionComplete.key)).isNotEmpty
  }

  @Test
  fun `can start a caseExecution with repetition rule manualStart anytime`() {
    val processInstance = startProcess()
    assertThat(processInstance.startWithRepetitionManualStart()).isNotEmpty
    assertThat(processInstance.startWithRepetitionManualStart()).isNotEmpty
  }

  @Test
  fun `can not start a caseExecution again without repetition rule`() {
    val processInstance = startProcess()
    assertThat(processInstance.startWithRepetitionNone()).isNotEmpty
    assertThat(processInstance.startWithRepetitionNone()).isEmpty
  }

  @Test
  fun `execute custom expression in listener`() {
    val processInstance = startProcess()
    processInstance.startWithRepetitionComplete()
  }

  class ExpressionSpikeListener : ExecutionListener {
    override fun notify(execution: DelegateExecution) {

      val expression = execution.expressionManager.createExpression("\${sentry.evaluate()}")
      val value = expression.getValue(execution, null)
      logger.info { """
        
        
        
        
        Hallo  ${expression.expressionText}
        
        $value
        
      """.trimIndent() }
    }

  }

  class SentryResolver {

    fun evaluate() : Boolean = false

  }

  private fun startProcess(): DummyCaseProcessInstance {
    val processInstance = process.start(Start())

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("keep_alive")

    return processInstance
  }
}
