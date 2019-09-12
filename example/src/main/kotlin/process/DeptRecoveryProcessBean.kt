package io.holunda.talk.camundacon.process

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.extension.casemanagement.CaseProcessBean
import io.holunda.extension.casemanagement.CaseProcessInstanceWrapper
import io.holunda.extension.casemanagement.EnumWithValue
import io.holunda.extension.casemanagement.command.StartCaseTaskCommand
import io.holunda.extension.casemanagement.command.StartProcessCommand
import mu.KLogging
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component(DeptRecoveryProcessBean.KEY)
class DeptRecoveryProcessBean(
  repositoryService: RepositoryService,
  runtimeService: RuntimeService
) : CaseProcessBean<StartDeptRecoveryProcessCommand, DeptRecoveryProcessInstance>(repositoryService = repositoryService, runtimeService = runtimeService) {
  companion object {
    const val KEY = "deptRecoveryProcess"
  }

  object VARIABLES {
    const val deptPaid = "deptPaid"
  }

  enum class CaseTasks : EnumWithValue {
    remindViaTelephone,
    remindViaMail,
    sendHelloLetter
    ;

    override val value = name
  }

  @Bean
  fun onHelloLetterSent() : ExecutionListener = ExecutionListener {
    val data : DeptRecoveryProcessData = it.getProcessData().copy(helloLetterSent = true)
    it.setVariable(DeptRecoveryProcessData.KEY, data)
  }

  @Bean
  fun onDeptPaid(): ExecutionListener = ExecutionListener {
    val paid = (it.getVariable(VARIABLES.deptPaid) as Boolean?)?:false
    val data : DeptRecoveryProcessData = it.getProcessData().copy(deptPaid = paid)
    it.setVariable(DeptRecoveryProcessData.KEY,data)
    it.removeVariable(VARIABLES.deptPaid)
  }

  override val key = KEY

  override fun wrap(processInstance: ProcessInstance): DeptRecoveryProcessInstance = DeptRecoveryProcessInstance(processInstance, runtimeService)
}

class DeptRecoveryProcessInstance(processInstance: ProcessInstance, runtimeService: RuntimeService) : CaseProcessInstanceWrapper(processInstance, runtimeService) {

}

/**
 * Command to start the process.
 */
data class StartDeptRecoveryProcessCommand(override val businessKey: String, val processData: DeptRecoveryProcessData) : StartProcessCommand {
  override val variables: VariableMap = Variables.putValue(DeptRecoveryProcessData.KEY, processData)
}


@Component
class ManuallyStartTaskDelegate(val process: DeptRecoveryProcessBean) : JavaDelegate {
  companion object : KLogging() {
    const val KEY = "doManualStart"
  }
  override fun execute(execution: DelegateExecution) {
    val task: DeptRecoveryProcessBean.CaseTasks = DeptRecoveryProcessBean.CaseTasks.valueOf(execution.getVariable(KEY) as String)

    process.startCaseTask(StartCaseTaskCommand(execution.processBusinessKey, task.name)).ifPresent{
      logger.info { "started caseTask: $it" }
    }



    execution.removeVariable(KEY)
  }

}
