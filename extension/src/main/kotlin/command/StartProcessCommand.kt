package io.holunda.extension.casemanagement.command

import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

/**
 * The start process command contains all information needed to start a CaseProcess.
 */
interface StartProcessCommand {
  val businessKey : String
  val variables: VariableMap
}