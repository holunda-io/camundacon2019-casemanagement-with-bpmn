package io.holunda.extension.casemanagement

import io.holunda.extension.casemanagement.command.StartCaseTaskCommand
import io.holunda.extension.casemanagement.command.StartProcessCommand
import io.holunda.extension.casemanagement.listener.CaseProcessStartListener
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.runtime.ProcessInstance
import java.lang.IllegalStateException

abstract class CaseProcess<C : StartProcessCommand, I : CaseProcessInstanceWrapper>(
    val runtimeService: RuntimeService,
    val repositoryService: RepositoryService
) {
  companion object {
    const val TASK_LIFECYCLE_KEY = "taskLifecycle"
  }

  object VARIABLES {
    val caseProcessDefinition = "caseProcessDefinition"
    val taskStageLifecycle = "taskStageLifecycle"
    val bpmnCaseExecutionEntities = "bpmnCaseExecutionEntities"
  }

  abstract val key: String

  fun start(cmd: C): I {
    val processInstance = runtimeService.startProcessInstanceByKey(key, cmd.businessKey, cmd.variables)

    return wrap(processInstance)
  }

  fun onStart(): ExecutionListener = CaseProcessStartListener()

  fun findByBusinessKey(businessKey:String) = runtimeService.createProcessInstanceQuery()
      .processInstanceBusinessKey(businessKey)
      .processDefinitionKey(key)
      .active()
      .singleResult()?:null

  fun loadForBusinessKey(businessKey:String) = findByBusinessKey(businessKey) ?: throw IllegalStateException("no processInstance found with key=$key and businessKey=$businessKey")

  fun startCaseTask(cmd: StartCaseTaskCommand) {

  }

  abstract fun wrap(processInstance: ProcessInstance): I
}
