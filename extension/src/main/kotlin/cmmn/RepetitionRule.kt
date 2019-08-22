package io.holunda.extension.casemanagement.cmmn

import io.holunda.extension.casemanagement.EnumWithValue

enum class RepetitionRule(override val value: String) : EnumWithValue {
  NONE("none"),
  COMPLETE("complete")
  ;

  companion object {
    const val KEY = "cmmnRepetitionRule"

    private val byValue = values().map { it.value to it }.toMap()

    fun byValue(map: Map<String,String>) = byValue(map[KEY])
    fun byValue(value:String?) = byValue[value]
  }

}