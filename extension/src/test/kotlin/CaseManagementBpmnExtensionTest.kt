package io.holunda.extension.casemanagement

import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.test.mock.MockExpressionManager
import java.util.ArrayList


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