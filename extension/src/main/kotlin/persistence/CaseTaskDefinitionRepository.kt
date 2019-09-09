package io.holunda.extension.casemanagement.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.extension.casemanagement.CaseProcess
import io.holunda.extension.casemanagement.CaseTaskKey
import io.holunda.extension.casemanagement.bpmn.CaseProcessDefinition
import io.holunda.extension.casemanagement.bpmn.CaseTaskDefinition
import org.camunda.bpm.engine.delegate.DelegateExecution

interface CaseTaskDefinitionReadOnlyRepository {
  fun load(): CaseProcessDefinition
  fun findByKey(caseTaskKey: String): CaseTaskDefinition

  fun findByKey(caseTaskKey: CaseTaskKey): CaseTaskDefinition = findByKey(caseTaskKey.key)
}

class CaseTaskDefinitionReadOnlyRepositoryFactory(private val om: ObjectMapper = jacksonObjectMapper()) {

  /**
   * Creates repository for use with delegateExecution inside a camunda listener or delegate.
   */
  fun create(execution: DelegateExecution) = object : CaseTaskDefinitionReadOnlyRepository {
    override fun load(): CaseProcessDefinition = om.readValue(execution.getVariable(CaseProcess.VARIABLES.caseProcessDefinition) as String)

    override fun findByKey(caseTaskKey: String): CaseTaskDefinition = load()[caseTaskKey]
  }
}
