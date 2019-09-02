package io.holunda.extension.casemanagement

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.command.StartCaseTaskCommand
import io.holunda.extension.casemanagement.command.StartProcessCommand
import io.holunda.extension.casemanagement.listener.CaseExecutionOnCompleteListener
import io.holunda.extension.casemanagement.listener.CaseExecutionOnStartListener
import io.holunda.extension.casemanagement.listener.CaseProcessStartListener
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.runtime.ProcessInstance
import java.lang.IllegalStateException

abstract class CaseProcess<C : StartProcessCommand, I : CaseProcessInstanceWrapper>(
    val runtimeService: RuntimeService,
    val repositoryService: RepositoryService,
    val om: ObjectMapper = jacksonObjectMapper()
) {
  companion object {
    const val TASK_LIFECYCLE_KEY = "taskLifecycle"
  }

  object VARIABLES {
    const val caseProcessDefinition = "caseProcessDefinition"
    const val taskStageLifecycle = "taskStageLifecycle"
    const val bpmnCaseExecutionEntities = "bpmnCaseExecutionEntities"
    const val caseExecutionId = "caseExecutionId"
  }

  abstract val key: String

  fun start(cmd: C): I {
    val processInstance = runtimeService.startProcessInstanceByKey(key, cmd.businessKey, cmd.variables)

    return wrap(processInstance)
  }

  fun onProcessStart(): ExecutionListener = CaseProcessStartListener()

  fun onCaseExecutionStart() : ExecutionListener = CaseExecutionOnStartListener(om)
  fun onCaseExecutionComplete() : ExecutionListener = CaseExecutionOnCompleteListener(om)

  fun findByBusinessKey(businessKey:String) : I? {
    val proc = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(businessKey)
        .processDefinitionKey(key)
        .active()
        .singleResult() ?: null

    return if (proc != null) {
      wrap(proc)
    } else {
      null
    }
  }

  fun loadForBusinessKey(businessKey:String) = findByBusinessKey(businessKey) ?: throw IllegalStateException("no processInstance found with key=$key and businessKey=$businessKey")

  fun startCaseTask(cmd: StartCaseTaskCommand) {
    findByBusinessKey(cmd.businessKey)?.startManually(cmd.taskKey)
  }

  abstract fun wrap(processInstance: ProcessInstance): I
}
