package io.holunda.extension.casemanagement

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.extension.casemanagement.bpmn.CaseProcessDefinition
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance

abstract class CaseProcessInstance(
    private val processInstance:ProcessInstance,
    private val runtimeService: RuntimeService,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) : ProcessInstance by processInstance {

  val caseProcessDefinition = objectMapper.readValue<CaseProcessDefinition>(runtimeService.getVariable(
      processInstanceId,
      CaseProcess.VARIABLES.caseProcessDefinition) as String
  )
}
