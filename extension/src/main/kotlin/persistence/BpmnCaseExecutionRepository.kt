package io.holunda.extension.casemanagement.persistence

import cmmn.BpmnCaseExecutionState


interface BpmnCaseExecutionRepository {
    fun findAll(): List<BpmnCaseExecutionEntity>
    fun save(execution: BpmnCaseExecutionEntity): BpmnCaseExecutionEntity

    fun findById(id: String): BpmnCaseExecutionEntity?
    fun findByCaseTaskKey(key: String): List<BpmnCaseExecutionEntity>
    fun count(): Int = findAll().size

    fun query(query: BpmnCaseExecutionQuery): List<BpmnCaseExecutionEntity>

    fun commit()
}

data class BpmnCaseExecutionQuery(
        val caseTaskKey: String? = null,
        val state: BpmnCaseExecutionState? = null
)