package io.holunda.extension.casemanagement.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.extension.casemanagement.CaseProcess
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.*


typealias BpmnCaseExecutionEntitiesStore = (List<BpmnCaseExecutionEntity>) -> Unit

/**
 * This gets stored a json serialized process variable.
 *
 * NOTE: might get to big and spoil history, in that case consider moving this to act_re_bytearray
 */
data class BpmnCaseExecutionProcessVariableRepository(
    val executions: MutableList<BpmnCaseExecutionEntity>,
    private val commit : BpmnCaseExecutionEntitiesStore
) : BpmnCaseExecutionRepository {

  override fun findByCaseTaskKey(key: String): List<BpmnCaseExecutionEntity>  = findAll().filter { it.caseTaskKey === key }
 // override fun findById(id: String): BpmnCaseExecutionEntity? = findAll().find {  it.id === id }
  override fun findById(id: String): BpmnCaseExecutionEntity? {
    for (e in executions) {
      val eId = e.id
      val match = id.equals(eId)

      if (match) {
        return e
      }
    }
    return null
  }
  override fun findAll(): List<BpmnCaseExecutionEntity> = executions.toList()

  override fun save(execution: BpmnCaseExecutionEntity): BpmnCaseExecutionEntity {
    return if (execution.transient) {
      val saved = execution.copy(id = UUID.randomUUID().toString())
      executions.add(saved)
      saved
    } else {
      if (findById(execution.id!!) == null) throw IllegalArgumentException("entity with id=${execution.id} does not exist")

      val i = executions.indexOfFirst { it.id == execution.id }
      executions[i] = execution
      execution
    }
  }


  /**
   *
   */
  override fun commit() {
    commit(executions)
  }

}

class BpmCaseExecutionRepositoryFactory(val om: ObjectMapper) {

  fun create(delegateExecution: DelegateExecution): BpmnCaseExecutionProcessVariableRepository {
    val list = (delegateExecution.getVariable(CaseProcess.VARIABLES.bpmnCaseExecutionEntities) as String?)
        ?.let { om.readValue<BpmnCaseExecutionEntities>(it) }
        ?: BpmnCaseExecutionEntities(mutableListOf())

    val commit: BpmnCaseExecutionEntitiesStore = {
      val json = om.writeValueAsString(BpmnCaseExecutionEntities(it))
      delegateExecution.setVariable(CaseProcess.VARIABLES.bpmnCaseExecutionEntities, json)
    }

    return BpmnCaseExecutionProcessVariableRepository(list.executions.toMutableList(), commit)
  }

}