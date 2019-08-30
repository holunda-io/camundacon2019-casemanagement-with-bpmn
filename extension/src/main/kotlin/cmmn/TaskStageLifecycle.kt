package io.holunda.extension.casemanagement.cmmn

data class TaskStageLifecycle(val tasks: Map<String, String>) {

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
