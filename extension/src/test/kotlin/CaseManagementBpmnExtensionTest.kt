package io.holunda.extension.casemanagement

import _test.DummyCaseProcess
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import io.holunda.extension.casemanagement._test.DualScenarioTest
import io.holunda.extension.casemanagement._test.stage.DummyCaseProcessGivenWhenStage
import io.holunda.extension.casemanagement._test.stage.DummyCaseProcessThenStage
import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.mock.MockExpressionManager
import org.junit.Rule
import java.util.*


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

}

