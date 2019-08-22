package io.holunda.extension.casemanagement.cmmn

import io.holunda.extension.casemanagement.EnumWithValue

enum class CmmnType(override val value : String) : EnumWithValue{
  HUMAN_TASK("humanTask")
  ;
  companion object {
    const val KEY = "cmmnType"

    private val byValue = values().map { it.value to it }.toMap()

    fun byValue(map: Map<String,String>) = byValue(map[KEY])
    fun byValue(value: String?) = byValue[value]
  }
}
