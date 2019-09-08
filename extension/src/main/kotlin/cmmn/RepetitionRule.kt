package io.holunda.extension.casemanagement.cmmn

import io.holunda.extension.casemanagement.CaseManagementBpmnExtension
import io.holunda.extension.casemanagement.EnumWithValue

enum class RepetitionRule(override val value: String) : EnumWithValue {
  /**
   * Default. No Repetition at all, you can only execute the task once.
   */
  NONE("none"),
  /**
   * A new instance of the task can start when the predecessor is finished.
   */
  COMPLETE("complete"),
  /**
   * A new instance of the task can start while another one is still running.
   */
  MANUAL_START("manualStart")
  ;

  companion object {
    private val byValue = values().map { it.value to it }.toMap()

    fun byValue(map: Map<String,String>) = byValue(map[CaseManagementBpmnExtension.ExtensionPropertyKeys.cmmnRepetitionRule])
    fun byValue(value:String?) = byValue[value]
  }

}

// these are all  available repetitionRules in the modeler
// complete
// create
// disable
// enable
// exit
// manualStart
// reenable
// start
