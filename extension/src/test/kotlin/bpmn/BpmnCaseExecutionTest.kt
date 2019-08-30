package io.holunda.extension.casemanagement.bpmn

import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.cmmn.CmmnType
import org.assertj.core.api.Assertions.assertThat
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import org.junit.Test
import java.util.*

class BpmnCaseExecutionTest {

  @Test
  fun `can be converted back and forth from json`() {
    val execution = BpmnCaseExecution(
        taskDefinition = CaseTaskDefinition(key = "task", name = "The Task", type = CmmnType.HUMAN_TASK, repetitionRule =  RepetitionRule.NONE, manualStart =  true, required = false),
        processInstanceId = UUID.randomUUID().toString(),
        processDefinitionId = "process:1:1"
    )

    val json = jacksonObjectMapper().writeValueAsString(execution)

    assertThat(jacksonObjectMapper().readValue<BpmnCaseExecution>(json)).isEqualTo(execution)
  }
}