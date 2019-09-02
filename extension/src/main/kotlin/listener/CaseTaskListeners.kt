package io.holunda.extension.casemanagement.listener

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.CaseProcess
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.omg.PortableInterceptor.ACTIVE


class CaseExecutionOnStartListener(
    val om: ObjectMapper = jacksonObjectMapper()
) : ExecutionListener {

  override fun notify(execution: DelegateExecution) {
    val caseExecutionId = execution.processInstance.getVariable(CaseProcess.VARIABLES.caseExecutionId) as String
    with(BpmCaseExecutionRepositoryFactory(om).create(execution.processInstance)) {
      val caseExecution = findById(caseExecutionId)!!
          .copy(
              executionId = execution.id,
              state = BpmnCaseExecutionState.ACTIVE
          )

      save(caseExecution)
      commit()
    }
  }
}

class CaseExecutionOnCompleteListener(
    val om: ObjectMapper = jacksonObjectMapper()
) : ExecutionListener {
  override fun notify(execution: DelegateExecution) {
    val caseExecutionId = execution.processInstance.getVariable(CaseProcess.VARIABLES.caseExecutionId) as String

    with(BpmCaseExecutionRepositoryFactory(om).create(execution.processInstance)) {
      save(findById(caseExecutionId)!!.copy(state = BpmnCaseExecutionState.COMPLETED))
      commit()
    }


  }

}