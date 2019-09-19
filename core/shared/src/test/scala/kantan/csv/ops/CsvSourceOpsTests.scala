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

package kantan.csv
package ops

import kantan.codecs.laws.CodecValue
import laws._
import laws.discipline.arbitrary._
import org.scalatest.Matchers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import scala.util.Try

class CsvSourceOpsTests extends AnyFunSuite with ScalaCheckPropertyChecks with Matchers {
  type TestCase = (Int, Float, String, Boolean)

  def compare[F, A](csv: List[Either[F, A]], data: List[RowValue[A]]): Unit = {
    csv.length should be(data.length)

    csv.zip(data).foreach {
      case (Right(is), CodecValue.LegalValue(_, cs)) => is should be(cs)
      case (Left(_), CodecValue.IllegalValue(_))     =>
      case (a, b)                                    => fail(s"$a is not compatible with $b")
    }
  }

  test("CsvSource instances should have a working asCsvReader method") {
    forAll { data: List[RowValue[TestCase]] =>
      compare(
        asCsv(data, rfc)
          .asCsvReader[TestCase](rfc)
          .toList,
        data
      )
    }
  }

  test("CsvSource instances should have a working readCsv method") {
    forAll { data: List[RowValue[TestCase]] =>
      compare(
        asCsv(data, rfc)
          .readCsv[List, TestCase](rfc),
        data
      )
    }
  }

  def compareUnsafe[A](csv: => List[A], data: List[RowValue[A]]): Unit = {
    def cmp(csv: List[A], data: List[RowValue[A]]): Unit = (csv, data) match {
      case (Nil, Nil)                                                 => ()
      case (h1 :: t1, CodecValue.LegalValue(_, h2) :: t2) if h1 == h2 => cmp(t1, t2)
      case (a, b)                                                     => fail(s"$a is not compatible with $b")
    }

    Try(csv) match {
      case scala.util.Success(is) => cmp(is, data)
      case _ =>
        data.filter(_.isIllegal).nonEmpty should be(true)
        ()
    }
  }

  test("CsvSource instances should have a working asUnsafeCsvReader method") {
    forAll { data: List[RowValue[TestCase]] =>
      compareUnsafe(
        asCsv(data, rfc)
          .asUnsafeCsvReader[TestCase](rfc)
          .toList,
        data
      )
    }
  }

  test("CsvSource instances should have a working unsafeReadCsv method") {
    forAll { data: List[RowValue[TestCase]] =>
      compareUnsafe(
        asCsv(data, rfc)
          .unsafeReadCsv[List, TestCase](rfc),
        data
      )
    }
  }
}
