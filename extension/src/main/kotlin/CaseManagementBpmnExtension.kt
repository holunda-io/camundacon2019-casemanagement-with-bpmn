package io.holunda.extension.casemanagement

import kotlin.reflect.KClass

// marker class for root package

interface EnumWithValue {
  val value : String
}

interface CaseTaskKey {
  val key: String
}

interface ActivityId {
  val key: String
}

