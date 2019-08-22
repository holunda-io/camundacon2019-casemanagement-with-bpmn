package io.holunda.extension.casemanagement.bpmn

import io.holunda.extension.casemanagement.cmmn.CmmnType
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.SubProcess
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties

private fun CamundaProperties.asMap() = this.camundaProperties.map { it.camundaName to it.camundaValue }.toMap()

/**
 * Helper to parse camunda extension properties
 */
data class CamundaCmmnProperties(
    val type: CmmnType?,
    val repetitionRule: RepetitionRule?
) {
  companion object {

    operator fun invoke(element: SubProcess): CamundaCmmnProperties = invoke(
        element.extensionElements
            .elementsQuery
            .filterByType(CamundaProperties::class.java)
            .singleResult()
    )

    operator fun invoke(properties: CamundaProperties): CamundaCmmnProperties = invoke(properties.asMap())
    operator fun invoke(properties: Map<String, String>): CamundaCmmnProperties = CamundaCmmnProperties(
        type = CmmnType.byValue(properties),
        repetitionRule = RepetitionRule.byValue(properties)
    )
  }
}


fun BpmnModelInstance.parseCaseDefinitions(): CaseProcessDefinition {

  val subProcesses = this.getModelElementsByType(SubProcess::class.java)
  val elements = mutableMapOf<String, CaseTaskDefinition>()

  for (subProcess in subProcesses) {
    val properties = CamundaCmmnProperties(subProcess)

    if (properties.type != null) {
      elements[subProcess.id] = CaseTaskDefinition(
          id = subProcess.id,
          name = subProcess.name,
          type = properties.type,
          repetitionRule = properties.repetitionRule ?: RepetitionRule.NONE,
          manualStart = false,
          required = false
      )
    }
  }

  return CaseProcessDefinition(elements.toMap())
}


data class CaseProcessDefinition(val tasks: Map<String, CaseTaskDefinition>)

data class CaseTaskDefinition(
    val id: String,
    val name: String,
    val type: CmmnType,
    val repetitionRule: RepetitionRule,
    val manualStart: Boolean,
    val required: Boolean
)
