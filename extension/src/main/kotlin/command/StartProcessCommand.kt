package io.holunda.extension.cmmn.command

import org.camunda.bpm.engine.variable.VariableMap

interface StartProcessCommand {
  val businessKey : String
  val variables: VariableMap
}