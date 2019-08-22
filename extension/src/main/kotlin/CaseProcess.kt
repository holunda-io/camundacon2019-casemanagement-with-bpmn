package io.holunda.extension.cmmn

import io.holunda.extension.cmmn.command.StartProcessCommand
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance

abstract class CaseProcess<C : StartProcessCommand, I : CaseProcessInstance>(
    private val runtimeService: RuntimeService
) {
  companion object {
    const val TASK_LIFECYCLE_KEY = "taskLifecycle"
  }

  abstract val key : String


  fun start(cmd: C): I {
    val processInstance = runtimeService.startProcessInstanceByKey(key, cmd.businessKey, cmd.variables)

    return wrap(processInstance)
  }


  abstract fun wrap(processInstance: ProcessInstance) : I
}
