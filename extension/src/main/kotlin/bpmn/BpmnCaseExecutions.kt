package io.holunda.extension.casemanagement.bpmn

import io.holunda.extension.casemanagement.CaseTaskKey

data class BpmnCaseExecutions(val executions: Map<String, List<BpmnCaseExecution>>) {


  class BpmnCaseExecutionsBuilder {
    private val executions: MutableMap<String, List<BpmnCaseExecution>> = mutableMapOf()


    fun add(execution: BpmnCaseExecution): BpmnCaseExecutionsBuilder {
      executions.putIfAbsent(execution.activityId, mutableListOf())



      return this;
    }


    fun build() = BpmnCaseExecutions(executions.toMap())
  }
}