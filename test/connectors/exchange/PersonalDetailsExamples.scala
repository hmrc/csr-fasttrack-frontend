/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors.exchange

import connectors.ExchangeObjects.PersonalDetails
import mappings.AddressExamples
import org.joda.time.LocalDate

object PersonalDetailsExamples {
  val FullDetails = PersonalDetails("firstName", "lastName", "preferredName", "email", LocalDate.now(), false,
    AddressExamples.FullAddress, Some("ABC123"), None, Some("1234567"), aLevel = true, stemLevel = true, civilServant = false,
    department = None)
}
