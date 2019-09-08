package io.holunda.extension.casemanagement

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import kotlin.reflect.KClass

object CaseManagementBpmnExtension {

  object ExtensionPropertyKeys {
    const val cmmnType = "cmmnType"
    const val cmmnRepetitionRule = "cmmnRepetitionRule"
  }


}

interface EnumWithValue {
  val value : String
}

interface CaseTaskKey : ActivityId{

  override val key: String
}

interface ActivityId {
  companion object {
    fun of(value: String) = object : ActivityId{
      override val key: String = value
    }
  }
  val key: String
}

val DelegateExecution.expressionManager get() = (this.processEngine.processEngineConfiguration as ProcessEngineConfigurationImpl).expressionManager
