package io.holunda.extension.casemanagement.command

data class StartCaseTaskCommand(
    val businessKey: String,
    val taskKey: String
)

internal fun StartCaseTaskCommand.withExecutionId(executionId:String) = StartCaseTaskWithExecutionIdCommand(
    this.businessKey,
    this.taskKey,
    executionId
)

internal data class StartCaseTaskWithExecutionIdCommand(
    val businessKey: String,
    val taskKey: String,
    val executionId: String
)