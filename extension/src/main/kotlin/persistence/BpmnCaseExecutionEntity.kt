package io.holunda.extension.casemanagement.persistence

import cmmn.BpmnCaseExecutionState

/**
 * When working with different executions, running subprocesses and state transitions,
 * more data is required, but for storage in a process variable, only need a bare minimum,
 * all additional data can be derived from the enclosing process.
 */
data class BpmnCaseExecutionEntity(
    /**
     * each execution gets a generated uuid. This does not depend on the actual state of the subprocess instance.
     */
    val id: String? = null,
    val version : Int = 0,
    /**
     * The technical id of the subprocess in the model. Needed to identify the definition meta data.
     */
    val caseTaskKey: String,
    /**
     * The camunda executionId of the actually running subprocess.
     * Can be null, since case executions are not automatically activated.
     *
     * For active or completed instances, this has to be set.
     */
    val executionId: String? = null,
    /**
     * The state as defined in CMMN state lifecycle.
     */
    val state: BpmnCaseExecutionState = BpmnCaseExecutionState.NEW
) {
  companion object {
    operator fun invoke(caseTaskKey: String) = BpmnCaseExecutionEntity(caseTaskKey = caseTaskKey)
  }

  val transient = id == null
}

/**
 * This is an abstraction of a list of executions, used to store as json.
 */
data class BpmnCaseExecutionEntities(val executions: List<BpmnCaseExecutionEntity>) {
  companion object {
    operator fun invoke(vararg executions: BpmnCaseExecutionEntity) = BpmnCaseExecutionEntities(executions.toList())
  }
}


