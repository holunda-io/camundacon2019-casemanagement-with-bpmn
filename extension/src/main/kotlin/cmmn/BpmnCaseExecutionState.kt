package cmmn

import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState

enum class BpmnCaseExecutionState(val state: CaseExecutionState) {
  NEW(CaseExecutionState.NEW),
  AVAILABLE(CaseExecutionState.AVAILABLE),
  ENABLED(CaseExecutionState.ENABLED),
  DISABLED(CaseExecutionState.DISABLED),
  ACTIVE(CaseExecutionState.ACTIVE),
  SUSPENDED(CaseExecutionState.SUSPENDED),
  TERMINATED(CaseExecutionState.TERMINATED),
  COMPLETED(CaseExecutionState.COMPLETED),
  FAILED(CaseExecutionState.FAILED),
  CLOSED(CaseExecutionState.CLOSED)
  ;

  val statusCode: Int by lazy {
    state.stateCode
  }
  val statusName: String by lazy {
    state.toString()
  }

  override fun toString(): String {
    return "BpmnCaseExecutionState(state=$state)"
  }
}
