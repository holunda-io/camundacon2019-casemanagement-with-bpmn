package io.holunda.extension.casemanagement.bpmn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.extension.casemanagement.cmmn.CmmnType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class BpmnCaseExecutionsTest  {
  companion object {
    val processInstanceId = UUID.randomUUID().toString()
    val processDefinitionId = "process:1:1"
  }

  private val objectMapper = jacksonObjectMapper()

  @Test
  fun `can convert from to json`() {
    val executions = BpmnCaseExecutions.BpmnCaseExecutionsBuilder()
        //.add(BpmnCaseExecution(CaseTaskDefinition("t1", "Task 1", CmmnType.HUMAN_TASK)))
        .build()

    val json = objectMapper.writeValueAsString(executions)

    assertThat(objectMapper.readValue<BpmnCaseExecutions>(json)).isEqualTo(executions)
  }
}