package io.holunda.extension.casemanagement.bpmn

import io.holunda.extension.casemanagement.cmmn.CmmnType
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.ExtensionElements
import org.camunda.bpm.model.bpmn.instance.SubProcess
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties

/**
 * Holds all information available by parsing the extension elements of an embedded subprocess in the
 * BPMN model that should behave as a case task.
 *
 * This configuration is not changed after the process is started and used by one or many (depending on the repetition rule)
 * concrete BpmnCaseExecutions that are started during the case (process) lifecycle.
 */
data class CaseTaskDefinition(
  val key: String,
  val name: String,
  val type: CmmnType,
  val repetitionRule: RepetitionRule,
  val manualStart: Boolean,
  val required: Boolean,
  val sentryOnPartExpression: String? = null
) {
  val hasSentry = sentryOnPartExpression != null

  val automaticStart = !manualStart
}

/**
 * This is the structure that is stored as json process variable.
 */
data class CaseProcessDefinition(val tasks: Map<String, CaseTaskDefinition>) {
  operator fun get(caseTaskKey: String) = tasks.get(caseTaskKey) ?: throw IllegalArgumentException("no definition found for key=$caseTaskKey")

  val values = tasks.values.toList()
}

private fun CamundaProperties.asMap() = this.camundaProperties.map { it.camundaName to it.camundaValue }.toMap()

/**
 * Helper to parse camunda extension properties
 */
private data class CamundaCmmnProperties(
  val type: CmmnType? = null,
  val repetitionRule: RepetitionRule? = null,
  val manualStart: Boolean? = null,
  val sentryOnPartExpression: String? = null
) {
  companion object {
    operator fun invoke(element: SubProcess): CamundaCmmnProperties {
      val elements: ExtensionElements? = element.extensionElements

      return if (elements != null)
        invoke(element.extensionElements
          .elementsQuery
          .filterByType(CamundaProperties::class.java)
          .singleResult())
      // if no extensions are set, this can not be a relevant case task
      else CamundaCmmnProperties()
    }

    operator fun invoke(properties: CamundaProperties): CamundaCmmnProperties = invoke(properties.asMap())
    operator fun invoke(properties: Map<String, String>): CamundaCmmnProperties = CamundaCmmnProperties(
      type = CmmnType.byValue(properties),
      repetitionRule = RepetitionRule.byValue(properties),
      manualStart = properties["cmmnManualStart"]?.toBoolean(),
      sentryOnPartExpression = properties["cmmnSentryOnPartExpression"]
    )
  }
}

/**
 * Parses extension elements of given bpmn model instance.
 */
internal fun BpmnModelInstance.parseCaseDefinitions(): CaseProcessDefinition {

  val subProcesses = this.getModelElementsByType(SubProcess::class.java)
  val elements = mutableMapOf<String, CaseTaskDefinition>()

  for (subProcess in subProcesses) {
    val properties = CamundaCmmnProperties(subProcess)

    if (properties.type != null) {
      elements[subProcess.id] = CaseTaskDefinition(
        key = subProcess.id,
        name = subProcess.name,
        type = properties.type,
        repetitionRule = properties.repetitionRule ?: RepetitionRule.NONE,
        manualStart = properties.manualStart ?: false,
        sentryOnPartExpression = properties.sentryOnPartExpression,
        required = false
      )
    }
  }

  return CaseProcessDefinition(elements.toMap())
}

