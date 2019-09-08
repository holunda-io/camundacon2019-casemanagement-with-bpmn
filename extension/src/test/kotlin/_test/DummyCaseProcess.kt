package _test

import io.holunda.extension.casemanagement.*
import io.holunda.extension.casemanagement.command.StartProcessCommand
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.variable.Variables
import java.util.*

class DummyCaseProcess(
    runtimeService: RuntimeService,
    repositoryService: RepositoryService
) : CaseProcess<Start, DummyCaseProcessInstance>(
    runtimeService,
    repositoryService
) {
  companion object {
    const val BPMN = "DummyCaseProcess.bpmn"
    const val KEY = "dummy_case_process"
  }


  enum class ProcessTask : ActivityId {
    keep_alive
    ;

    override val key = name
  }

  enum class CaseTask : CaseTaskKey {
    runAutomatically_repetitionNone,
    manualStart_repetitionNone,
    manualStart_repetitionComplete,
    manualStart_repetitionManualStart,
    manualStart_repetitionComplete_withSentry
    ;

    override val key = name
  }

  enum class Elements(override val key: String) : ActivityId {
    USERTASK_MS_REPETITION_COMPLETE("ut_manualStart_repetitionComplete"),
    USERTASK_MS_REPETITION_COMPLETE_WITH_SENTRY("ut_manualStart_repetitionComplete_withSentry"),
  }

  override fun wrap(processInstance: ProcessInstance): DummyCaseProcessInstance = DummyCaseProcessInstance(processInstance, runtimeService)
  override val key: String = KEY
}

data class Start(override val businessKey: String = UUID.randomUUID().toString()) : StartProcessCommand {
  override val variables = Variables.createVariables()!!
}

class DummyCaseProcessInstance(processInstance: ProcessInstance, runtimeService: RuntimeService) : CaseProcessInstanceWrapper(processInstance, runtimeService) {

  fun startWithRepetitionNone() = startManually(DummyCaseProcess.CaseTask.manualStart_repetitionNone)
  fun startWithRepetitionComplete() = startManually(DummyCaseProcess.CaseTask.manualStart_repetitionComplete)
  fun startWithRepetitionManualStart() = startManually(DummyCaseProcess.CaseTask.manualStart_repetitionManualStart)

}

/**
 * Helper to set the result of sentry evaluation for task with sentry
 */
class ManualStartRepetitionCompleteWithSentryResolver(var state: Boolean = false) {
  @Suppress(names = ["UNUSED", "UNUSED_PARAMETER"]) // only by expression in bpmn model
  fun evaluate(execution: DelegateExecution) = state
}
