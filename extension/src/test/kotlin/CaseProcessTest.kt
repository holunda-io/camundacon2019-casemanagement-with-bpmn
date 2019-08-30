package io.holunda.extension.casemanagement

import _test.DummyCaseProcess
import _test.Start
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.extension.mockito.CamundaMockito
import org.junit.Rule
import org.junit.Test

@Deployment(resources = [DummyCaseProcess.BPMN])
class CaseProcessTest {

  @get:Rule
  val camunda = ProcessEngineRule(CamundaTestConfiguration().buildProcessEngine()).apply {
    BpmnAwareTests.init(this.processEngine)
  }

  val process: DummyCaseProcess by lazy {
    DummyCaseProcess(camunda.runtimeService, camunda.repositoryService).apply {
      CamundaMockito.registerInstance(this)
    }
  }

  @Test
  fun `start process`() {
    val processInstance = process.start(Start())

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("keep_alive")

    val def = processInstance.caseProcessDefinition

    assertThat(def.tasks["runAutomatically_repetitionNone"]!!.repetitionRule).isEqualTo(RepetitionRule.NONE)
  }
}


