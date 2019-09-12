package io.holunda.extension.casemanagement

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.command.StartCaseTaskCommand
import io.holunda.extension.casemanagement.command.StartProcessCommand
import io.holunda.extension.casemanagement.listener.CaseExecutionOnCompleteListener
import io.holunda.extension.casemanagement.listener.CaseExecutionOnStartListener
import io.holunda.extension.casemanagement.listener.CaseProcessStartListener
import io.holunda.extension.casemanagement.listener.ReevaluateSentriesDelegate
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.runtime.ProcessInstance
import java.util.*


interface CaseProcess<C : StartProcessCommand, I : CaseProcessInstanceWrapper> {

  fun start(cmd: C): I

  fun findByBusinessKey(businessKey: String): Optional<I>
}

abstract class CaseProcessBean<C : StartProcessCommand, I : CaseProcessInstanceWrapper>(
  val runtimeService: RuntimeService,
  val repositoryService: RepositoryService
) : CaseProcess<C, I> {
  companion object {
    const val SUBPROCESS_SENTRY_REEVALUATION = "subprocess_sentry_reevaluation"
  }

  object VARIABLES {
    const val caseProcessDefinition = "caseProcessDefinition"
    const val bpmnCaseExecutionEntities = "bpmnCaseExecutionEntities"
    const val caseExecutionId = "caseExecutionId"
  }

  abstract val key: String

  override fun start(cmd: C): I {
    val processInstance = runtimeService.startProcessInstanceByKey(key, cmd.businessKey, cmd.variables)

    return wrap(processInstance)
  }

  fun onProcessStart(): ExecutionListener = CaseProcessStartListener()

  fun onCaseExecutionStart(): ExecutionListener = CaseExecutionOnStartListener()
  fun onCaseExecutionComplete(): ExecutionListener = CaseExecutionOnCompleteListener()
  fun onReevaluateSentries(): JavaDelegate = ReevaluateSentriesDelegate()

  override fun findByBusinessKey(businessKey: String): Optional<I> {
    val proc: ProcessInstance? = runtimeService.createProcessInstanceQuery()
      .processInstanceBusinessKey(businessKey)
      .processDefinitionKey(key)
      .active()
      .singleResult()

    return Optional.ofNullable(proc).map { wrap(it) }
  }

  fun loadForBusinessKey(businessKey: String): I = findByBusinessKey(businessKey).orElseThrow {  IllegalStateException("no processInstance found with key=$key and businessKey=$businessKey") }

  fun startCaseTask(cmd: StartCaseTaskCommand) : Optional<String> = findByBusinessKey(cmd.businessKey).flatMap { it.startManually(cmd.taskKey) }


  abstract fun wrap(processInstance: ProcessInstance): I
}
