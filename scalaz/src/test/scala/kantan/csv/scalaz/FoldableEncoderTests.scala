/*
 * Copyright 2015 Nicolas Rinaudo
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

package kantan.csv.scalaz

import arbitrary._
import kantan.codecs.scalaz.laws.discipline.scalatest.ScalazSuite
import kantan.csv.RowEncoder
import kantan.csv.laws.discipline.RowEncoderTests
import org.scalacheck.{Arbitrary, Gen}
import scalaz.std.list._

class FoldableEncoderTests extends ScalazSuite {
  implicit val arb: Arbitrary[List[Int]]      = Arbitrary(Gen.nonEmptyListOf(Arbitrary.arbitrary[Int]))
  implicit val encoder: RowEncoder[List[Int]] = foldableRowEncoder[List, Int]

  checkAll("Foldable[Int]", RowEncoderTests[List[Int]].encoder[List[Byte], List[Float]])
}