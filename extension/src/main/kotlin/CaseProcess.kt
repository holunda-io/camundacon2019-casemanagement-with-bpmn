package io.holunda.extension.casemanagement

import io.holunda.extension.casemanagement.listener.CaseProcessStartListener
import io.holunda.extension.cmmn.command.StartProcessCommand
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.runtime.ProcessInstance

abstract class CaseProcess<C : StartProcessCommand, I : CaseProcessInstance>(
    val runtimeService: RuntimeService,
    val repositoryService: RepositoryService
) {
  companion object {
    const val TASK_LIFECYCLE_KEY = "taskLifecycle"
  }

  object VARIABLES {
    val caseProcessDefinition = "caseProcessDefinition"
  }

  abstract val key: String

  fun start(cmd: C): I {
    val processInstance = runtimeService.startProcessInstanceByKey(key, cmd.businessKey, cmd.variables)

    //val createCaseProcessDefinition = repositoryService.createCaseProcessDefinition(processInstance.processDefinitionId)

    return wrap(processInstance)
  }

  fun onStart(): ExecutionListener = CaseProcessStartListener()

  abstract fun wrap(processInstance: ProcessInstance): I
}
