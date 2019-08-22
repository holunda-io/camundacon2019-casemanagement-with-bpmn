package io.holunda.extension.casemanagement.cmmn

import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState

data class TaskStageLifecycle(val tasks: Map<String, String>) {

}

enum class CaseTaskState(val state: CaseExecutionState) {
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
}
//
//val NEW: CaseExecutionState = CaseExecutionStateImpl(0, "new")
//val AVAILABLE: CaseExecutionState = CaseExecutionStateImpl(1, "available")
//val ENABLED: CaseExecutionState = CaseExecutionStateImpl(2, "enabled")
//val DISABLED: CaseExecutionState = CaseExecutionStateImpl(3, "disabled")
//val ACTIVE: CaseExecutionState = CaseExecutionStateImpl(4, "active")
//val SUSPENDED: CaseExecutionState = CaseExecutionStateImpl(5, "suspended")
//val TERMINATED: CaseExecutionState = CaseExecutionStateImpl(6, "terminated")
//val COMPLETED: CaseExecutionState = CaseExecutionStateImpl(7, "completed")
//val FAILED: CaseExecutionState = CaseExecutionStateImpl(8, "failed")
//val CLOSED: CaseExecutionState = CaseExecutionStateImpl(9, "closed")
