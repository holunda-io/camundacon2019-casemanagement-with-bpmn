package io.holunda.extension.cmmn

import org.camunda.bpm.engine.runtime.ProcessInstance

abstract class CaseProcessInstance(
    val processInstance:ProcessInstance
) {
  val businessKey: String = processInstance.businessKey
}
