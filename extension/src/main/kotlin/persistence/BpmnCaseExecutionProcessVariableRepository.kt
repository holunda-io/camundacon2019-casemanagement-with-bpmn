package io.holunda.extension.casemanagement.persistence

import io.holunda.extension.casemanagement.CaseProcessBean
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.variable.value.ObjectValue
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

class BpmCaseExecutionRepositoryFactory {


  fun create(delegateExecution: DelegateExecution): BpmnCaseExecutionProcessVariableRepository {
    fun  initList(): BpmnCaseExecutionEntities {
      val value: BpmnCaseExecutionEntities? = delegateExecution.getVariable(CaseProcessBean.VARIABLES.bpmnCaseExecutionEntities) as BpmnCaseExecutionEntities?
      return  value?: BpmnCaseExecutionEntities(mutableListOf())
    }


    val commit: BpmnCaseExecutionEntitiesStore = {
      delegateExecution.setVariable(CaseProcessBean.VARIABLES.bpmnCaseExecutionEntities, BpmnCaseExecutionEntities(it))
    }

    return BpmnCaseExecutionProcessVariableRepository(initList().executions.toMutableList(), commit)
  }

  fun create(runtimeService: RuntimeService, processInstanceId: String): BpmnCaseExecutionProcessVariableRepository {
    fun  initList(): BpmnCaseExecutionEntities {
      val value: BpmnCaseExecutionEntities? = runtimeService.getVariable(processInstanceId, CaseProcessBean.VARIABLES.bpmnCaseExecutionEntities) as BpmnCaseExecutionEntities?
      return  value?: BpmnCaseExecutionEntities(mutableListOf())
    }
    val commit: BpmnCaseExecutionEntitiesStore = {
      runtimeService.setVariable(processInstanceId, CaseProcessBean.VARIABLES.bpmnCaseExecutionEntities, it)
    }

    return BpmnCaseExecutionProcessVariableRepository(initList().executions.toMutableList(), commit)
  }

}
