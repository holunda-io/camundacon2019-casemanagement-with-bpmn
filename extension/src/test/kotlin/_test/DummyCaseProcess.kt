package _test

import io.holunda.extension.casemanagement.CaseProcess
import io.holunda.extension.casemanagement.CaseProcessInstance
import io.holunda.extension.casemanagement.CaseProcessInstanceWrapper
import io.holunda.extension.casemanagement.CaseTaskKey
import io.holunda.extension.casemanagement.command.StartProcessCommand
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
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

  enum class CaseTasks : CaseTaskKey {
    runAutomatically_repetitionNone,
    manualStart_repetitionNone,
    manualStart_repetitionComplete
    ;

    override val key = name
  }


  override fun wrap(processInstance: ProcessInstance): DummyCaseProcessInstance = DummyCaseProcessInstance(processInstance, runtimeService)
  override val key: String = KEY


}

data class Start(override val businessKey: String = UUID.randomUUID().toString()) : StartProcessCommand {
  override val variables = Variables.createVariables()!!
}

class DummyCaseProcessInstance(processInstance: ProcessInstance, runtimeService: RuntimeService) : CaseProcessInstanceWrapper(processInstance, runtimeService)
