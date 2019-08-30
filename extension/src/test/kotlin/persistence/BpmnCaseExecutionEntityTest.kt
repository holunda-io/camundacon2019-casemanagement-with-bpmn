package io.holunda.extension.casemanagement.persistence

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BpmnCaseExecutionEntityTest {

  private val om = jacksonObjectMapper()

  @Test
  fun `new entity is transient and has initial state`() {
    val entity = BpmnCaseExecutionEntity("foo")

    assertThat(entity.id).isNull()
    assertThat(entity.version).isEqualTo(0)
    assertThat(entity.state).isEqualTo(BpmnCaseExecutionState.NEW)
    assertThat(entity.transient).isTrue()
  }

  @Test
  fun `can convert entity to from json`() {
    val original = BpmnCaseExecutionEntity(caseTaskKey = "foo")

    val json = om.writeValueAsString(original)

    val fromJson : BpmnCaseExecutionEntity = om.readValue(json)

    assertThat(fromJson).isEqualTo(original)
  }

  @Test
  fun `can convert entities to from json`() {
    val e1 = BpmnCaseExecutionEntity(id = "1", caseTaskKey = "foo")
    val e2 = BpmnCaseExecutionEntity(id = "2", caseTaskKey = "foo")


    val original = BpmnCaseExecutionEntities(e1,e2)

    val json = om.writeValueAsString(original)

    val fromJson : BpmnCaseExecutionEntities = om.readValue(json)

    assertThat(fromJson).isEqualTo(original)
  }




}