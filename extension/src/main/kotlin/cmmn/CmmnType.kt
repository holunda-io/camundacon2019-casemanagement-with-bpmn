package io.holunda.extension.casemanagement.cmmn

import io.holunda.extension.casemanagement.CaseManagementBpmnExtension
import io.holunda.extension.casemanagement.EnumWithValue

enum class CmmnType(override val value : String) : EnumWithValue{
  HUMAN_TASK("humanTask"),
  PROCESS_TASK("processTask"),
  ;

  companion object {
    private val byValue = values().map { it.value to it }.toMap()

    fun byValue(map: Map<String,String>) = byValue(map[CaseManagementBpmnExtension.ExtensionPropertyKeys.cmmnType])
    fun byValue(value: String?) = byValue[value]
  }
}
