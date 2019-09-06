package io.holunda.extension.casemanagement

import _test.DummyCaseProcess
import _test.DummyCaseProcessInstance
import _test.Start
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.reflect.TypeToken
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.base.ScenarioTestBase
import com.tngtech.jgiven.junit.ScenarioTest
import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.mock.MockExpressionManager
import org.camunda.bpm.extension.mockito.CamundaMockito
import org.junit.Rule
import java.util.ArrayList
import com.tngtech.jgiven.impl.Scenario
import com.tngtech.jgiven.junit.JGivenMethodRule
import com.tngtech.jgiven.junit.JGivenClassRule
import io.holunda.extension.casemanagement._test.stage.DummyCaseProcessGivenWhenStage
import io.holunda.extension.casemanagement._test.stage.DummyCaseProcessThenStage
import org.junit.ClassRule


internal class CamundaTestConfiguration : StandaloneInMemProcessEngineConfiguration() {
  init {
    expressionManager = MockExpressionManager()
    jobExecutorActivate = false
    isMetricsEnabled = false
    isDbMetricsReporterActivate = false

    history = ProcessEngineConfiguration.HISTORY_FULL
    databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE

    customPostBPMNParseListeners = ArrayList()
    setCustomJobHandlers(ArrayList())
  }
}

@Deployment(resources = [DummyCaseProcess.BPMN])
abstract class AbstractDummyCaseProcessTest : DualScenarioTest<DummyCaseProcessGivenWhenStage, DummyCaseProcessThenStage>() {
  companion object : KLogging() {
    val om = jacksonObjectMapper()
  }

  @ProvidedScenarioState
  @get:Rule
  val camunda = ProcessEngineRule(CamundaTestConfiguration().buildProcessEngine()).apply {
    BpmnAwareTests.init(this.processEngine)
  }

  val process: DummyCaseProcess by lazy {
    DummyCaseProcess(camunda.runtimeService, camunda.repositoryService).apply {
      CamundaMockito.registerInstance(this)
      CamundaMockito.registerInstance("expressionSpike", CaseProcessTest.ExpressionSpikeListener())
      CamundaMockito.registerInstance("sentry", CaseProcessTest.SentryResolver())
    }
  }

  private fun startProcess(): DummyCaseProcessInstance {
    val processInstance = process.start(Start())
    BpmnAwareTests.assertThat(processInstance).isWaitingAt("keep_alive")

    return processInstance
  }
}



abstract class DualScenarioTestBase<GIVEN_WHEN, THEN> : ScenarioTestBase<GIVEN_WHEN, GIVEN_WHEN, THEN>() {

  @Suppress("UNCHECKED_CAST")
  override fun createScenario(): Scenario<GIVEN_WHEN, GIVEN_WHEN, THEN> {
    val givenWhenClass = object : TypeToken<GIVEN_WHEN>(javaClass) {}.rawType as Class<GIVEN_WHEN>
    val thenClass = object : TypeToken<THEN>(javaClass) {}.rawType as Class<THEN>

    return Scenario(givenWhenClass, givenWhenClass, thenClass)
  }

}


open class DualScenarioTest<GIVEN_WHEN, THEN> : DualScenarioTestBase<GIVEN_WHEN, THEN>() {

  companion object {
    @get: ClassRule
    @JvmStatic
    val writerRule = JGivenClassRule()
  }

  @Suppress("UNCHECKED_CAST")
  override fun getScenario(): Scenario<GIVEN_WHEN, GIVEN_WHEN, THEN> = scenarioRule.scenario as Scenario<GIVEN_WHEN, GIVEN_WHEN, THEN>


  @get: Rule
  val scenarioRule = JGivenMethodRule(createScenario())


}
