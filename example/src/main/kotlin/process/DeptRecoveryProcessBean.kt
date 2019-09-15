package io.holunda.talk.camundacon.process

import io.holunda.extension.casemanagement.CaseProcessBean
import io.holunda.extension.casemanagement.CaseProcessInstanceWrapper
import io.holunda.extension.casemanagement.EnumWithValue
import io.holunda.extension.casemanagement.command.StartProcessCommand
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.springframework.context.annotation.Bean
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

class DeptRecoveryProcessInstance(
  processInstance: ProcessInstance,
  runtimeService: RuntimeService
) : CaseProcessInstanceWrapper(processInstance, runtimeService) {

  val data: DeptRecoveryProcessData get() = runtimeService.getVariable(processInstanceId, DeptRecoveryProcessData.KEY) as DeptRecoveryProcessData
}

/**
 * Command to start the process.
 */
data class StartDeptRecoveryProcessCommand(override val businessKey: String, val processData: DeptRecoveryProcessData) : StartProcessCommand {
  companion object {
    operator fun invoke(businessKey: String) = StartDeptRecoveryProcessCommand(businessKey, DeptRecoveryProcessData(businessKey))
  }

  override val variables: VariableMap = Variables.putValue(DeptRecoveryProcessData.KEY, processData)
}
