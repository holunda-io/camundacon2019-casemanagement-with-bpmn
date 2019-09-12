package io.holunda.talk.camundacon.process

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.extension.casemanagement.CaseProcessBean
import io.holunda.extension.casemanagement.CaseProcessInstanceWrapper
import io.holunda.extension.casemanagement.EnumWithValue
import io.holunda.extension.casemanagement.command.StartCaseTaskCommand
import io.holunda.extension.casemanagement.command.StartProcessCommand
import io.holunda.talk.camundacon.Json
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
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component(DeptRecoveryProcessBean.KEY)
class DeptRecoveryProcessBean(
  repositoryService: RepositoryService,
  runtimeService: RuntimeService,
  om: ObjectMapper
) : CaseProcessBean<StartDeptRecoveryProcessCommand, DeptRecoveryProcessInstance>(repositoryService = repositoryService, runtimeService = runtimeService, om = om) {
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
  fun onHelloLetterSent(om:ObjectMapper) : ExecutionListener = ExecutionListener {
    val data : DeptRecoveryProcessData = it.getProcessData(om).copy(deptPaid = true)
    it.setVariable(DeptRecoveryProcessData.KEY, om.writeValueAsString(data))
  }

  @EventListener(condition = """#e.getCurrentActivityId().equals("milestone_deptPaid") && #e.getEventName().equals("end") """)
  fun onDeptPaid(e: DelegateExecution) {
    val paid = (e.getVariable(VARIABLES.deptPaid) as Boolean?)?:false
    val data : DeptRecoveryProcessData = e.getProcessData(om).copy(deptPaid = paid)
    e.setVariable(DeptRecoveryProcessData.KEY, om.writeValueAsString(data))
    e.removeVariable(VARIABLES.deptPaid)
  }

  override val key = KEY

  override fun wrap(processInstance: ProcessInstance): DeptRecoveryProcessInstance = DeptRecoveryProcessInstance(processInstance, runtimeService, om)
}

class DeptRecoveryProcessInstance(processInstance: ProcessInstance, runtimeService: RuntimeService, om: ObjectMapper) : CaseProcessInstanceWrapper(processInstance, runtimeService, om) {

}

/**
 * Command to start the process.
 */
data class StartDeptRecoveryProcessCommand(override val businessKey: String, val processData: Json) : StartProcessCommand {
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
