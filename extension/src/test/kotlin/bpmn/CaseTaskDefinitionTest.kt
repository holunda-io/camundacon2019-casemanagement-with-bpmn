package io.holunda.extension.casemanagement.bpmn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.extension.casemanagement.cmmn.CmmnType
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.Test
import sun.lwawt.macosx.CPrinterDevice

class CaseTaskDefinitionTest {


  @Test
  fun `parse case definitions`() {
    val modelInstance = Bpmn.readModelFromStream(CaseTaskDefinitionTest::class.java.getResourceAsStream("/DummyCaseProcess.bpmn"))

    val caseDefinitions = modelInstance.parseCaseDefinitions()
    println(caseDefinitions)

    assertThat(caseDefinitions.tasks).hasSize(3)

    assertThat(caseDefinitions.tasks.get("manualStart_repetitionComplete")!!.repetitionRule).isEqualTo(RepetitionRule.COMPLETE)
    assertThat(caseDefinitions.tasks.get("runAutomatically_repetitionNone")!!.repetitionRule).isEqualTo(RepetitionRule.NONE)
  }


  @Test
  fun `convert json`() {
    val objectMapper = jacksonObjectMapper()
    val task = CaseTaskDefinition(id = "1", name = "task", type = CmmnType.HUMAN_TASK, repetitionRule = RepetitionRule.COMPLETE, manualStart = false, required = false)

    val def = CaseProcessDefinition(mapOf("1" to task))

    val json = objectMapper.writeValueAsString(def)

    println("---- $json")

    assertThat(objectMapper.readValue<CaseProcessDefinition>(json)).isEqualTo(def)
  }
}