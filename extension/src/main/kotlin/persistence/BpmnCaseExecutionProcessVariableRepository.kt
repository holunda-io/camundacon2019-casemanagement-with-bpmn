package io.holunda.extension.casemanagement.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.extension.casemanagement.CaseProcess
import org.camunda.bpm.engine.RuntimeService
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
    private val commit: BpmnCaseExecutionEntitiesStore
) : BpmnCaseExecutionRepository {

  override fun query(query: BpmnCaseExecutionQuery): List<BpmnCaseExecutionEntity> = findAll().filter {
    nullOrEqual(it.caseTaskKey, query.caseTaskKey) && nullOrEqual(it.state, query.state)
  }

  override fun findByCaseTaskKey(key: String): List<BpmnCaseExecutionEntity> = query(BpmnCaseExecutionQuery(caseTaskKey = key))
  override fun findById(id: String): BpmnCaseExecutionEntity? = findAll().find { it.id == id }
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


  /**
   * Null safe comparision.
   */
  private fun nullOrEqual(value: Any, param: Any?) = (param ?: value) == value
}

class BpmCaseExecutionRepositoryFactory(val om: ObjectMapper) {

  private fun initEntities(json: Any?) = json
      ?.let { it as String }
      ?.let { om.readValue<BpmnCaseExecutionEntities>(it) }
      ?: BpmnCaseExecutionEntities(mutableListOf())

  fun create(delegateExecution: DelegateExecution): BpmnCaseExecutionProcessVariableRepository {
    val list = initEntities(delegateExecution.getVariable(CaseProcess.VARIABLES.bpmnCaseExecutionEntities))

    val commit: BpmnCaseExecutionEntitiesStore = {
      val json = om.writeValueAsString(BpmnCaseExecutionEntities(it))
      delegateExecution.setVariable(CaseProcess.VARIABLES.bpmnCaseExecutionEntities, json)
    }

    return BpmnCaseExecutionProcessVariableRepository(list.executions.toMutableList(), commit)
  }

  fun create(runtimeService: RuntimeService, processInstanceId: String): BpmnCaseExecutionProcessVariableRepository {
    val list = initEntities(runtimeService.getVariable(processInstanceId, CaseProcess.VARIABLES.bpmnCaseExecutionEntities))

    val commit: BpmnCaseExecutionEntitiesStore = {
      val json = om.writeValueAsString(BpmnCaseExecutionEntities(it))
      runtimeService.setVariable(processInstanceId, CaseProcess.VARIABLES.bpmnCaseExecutionEntities, json)
    }

    return BpmnCaseExecutionProcessVariableRepository(list.executions.toMutableList(), commit)
  }

}
