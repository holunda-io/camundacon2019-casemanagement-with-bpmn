package io.holunda.extension.casemanagement.bpmn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.extension.casemanagement.cmmn.CmmnType
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.Test

class CaseTaskDefinitionTest {


  @Test
  fun `parse case definitions`() {
    val modelInstance = Bpmn.readModelFromStream(CaseTaskDefinitionTest::class.java.getResourceAsStream("/DummyCaseProcess.bpmn"))

    val caseDefinitions = modelInstance.parseCaseDefinitions()

    assertThat(caseDefinitions.caseTaskDefinitions).hasSize(5)

    assertThat(caseDefinitions.get("manualStart_repetitionComplete").repetitionRule).isEqualTo(RepetitionRule.COMPLETE)
    assertThat(caseDefinitions.get("manualStart_repetitionManualStart").repetitionRule).isEqualTo(RepetitionRule.MANUAL_START)
    assertThat(caseDefinitions.get("runAutomatically_repetitionNone").repetitionRule).isEqualTo(RepetitionRule.NONE)
  }


  @Test
  fun `convert json`() {
    val objectMapper = jacksonObjectMapper()
    val task = CaseTaskDefinition(key = "1", name = "task", type = CmmnType.HUMAN_TASK, repetitionRule = RepetitionRule.COMPLETE, manualStart = false, required = false)

    val def = CaseProcessDefinition(setOf(task))

    val json = objectMapper.writeValueAsString(def)

    assertThat(objectMapper.readValue<CaseProcessDefinition>(json)).isEqualTo(def)
  }
}
