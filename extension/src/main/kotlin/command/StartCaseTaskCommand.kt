package io.holunda.extension.casemanagement.command

import io.holunda.extension.casemanagement.CaseTaskKey

data class StartCaseTaskCommand(
    val businessKey: String,
    val taskKey: String
) {
  companion object {
    operator fun invoke(businessKey:String, key: CaseTaskKey) = StartCaseTaskCommand(businessKey, key.key)
  }
}
