package io.holunda.extension.casemanagement.persistence


interface BpmnCaseExecutionRepository {
  fun findAll(): List<BpmnCaseExecutionEntity>
  fun save(execution: BpmnCaseExecutionEntity): BpmnCaseExecutionEntity

  fun findById(id: String) : BpmnCaseExecutionEntity?
  fun findByCaseTaskKey(key: String) : List<BpmnCaseExecutionEntity>
  fun count(): Int = findAll().size

  fun commit()
}
