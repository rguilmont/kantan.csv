/*
 * Copyright 2017 Nicolas Rinaudo
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

package kantan.csv.engine

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import kantan.codecs.resource.ResourceIterator
import kantan.csv._

/** Provides CSV reader and writer engines using [[https://github.com/FasterXML/jackson-dataformat-csv jackson.csv]].
  *
  * Importing `kantan.csv.engine.jackson._` will replace default engines by the jackson-backed ones. If you need to
  * tweak how jackson.csv behaves, however, you can handcraft engines though [[readerEngineFrom]] and
  * [[writerEngineFrom]] - all you need is a function that knows how to turn a column separator character in an instance
  * of `CsvSchema`.
  */
package object jackson {
  // - Schema ---------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Type of functions that create a `CSVSchema` instance from a given column separator. */
  type CSVSchemaBuilder = Char ⇒ CsvSchema


  // - Reader engines --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Creates a new [[ReaderEngine]] from the specified [[CSVSchemaBuilder]].
    *
    * The purpose of this is to let developers use some of the jackson.csv features that kantan.csv does not expose
    * through its public API.
    *
    * For example, the following declares a jackson-backed [[ReaderEngine]] that uses `#` as a quote character:
    * {{{
    * scala> import kantan.csv.ops._
    * scala> import kantan.csv.engine.jackson.readerEngineFrom
    * scala> import kantan.csv.engine.jackson.JacksonCsv.defaultParserSchema
    *
    * scala> implicit val readerEngine = readerEngineFrom(s ⇒ defaultParserSchema(s).withQuoteChar('#'))
    *
    * scala> "#a##b#,cd".readCsv[List, List[String]](',', false)
    * res0: List[kantan.csv.ReadResult[List[String]]] = List(Success(List(a#b, cd)))
    * }}}
    */
  def readerEngineFrom(f: CSVSchemaBuilder): ReaderEngine =
    ReaderEngine { (r, s) ⇒ ResourceIterator.fromIterator(JacksonCsv.parse(r, f(s))) }

  /** Default jackson.csv [[ReaderEngine]].
    *
    * It's possible to tweak the behaviour of the underlying writer through [[readerEngineFrom]].
    */
  implicit val jacksonCsvReaderEngine: ReaderEngine = readerEngineFrom(s ⇒ JacksonCsv.defaultParserSchema(s))



  // - Writer engines --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Creates a new [[WriterEngine]] from the specified [[CSVSchemaBuilder]].
    *
    * The purpose of this is to let developers use some of the jackson.csv features that kantan.csv does not expose
    * through its public API.
    *
    * For example, the following declares a jackson-backed [[WriterEngine]] that uses `#` as a quote character:
    * {{{
    * scala> import kantan.csv.ops._
    * scala> import kantan.csv.engine.jackson.writerEngineFrom
    * scala> import kantan.csv.engine.jackson.JacksonCsv.defaultParserSchema
    *
    * scala> implicit val writerEngine = writerEngineFrom(s ⇒ defaultParserSchema(s).withQuoteChar('#'))
    *
    * scala> List(List("a#b", "cd")).asCsv(',')
    * res0: String =
    * "#a##b#,cd
    * "
    * }}}
    */
  def writerEngineFrom(f: CSVSchemaBuilder): WriterEngine = WriterEngine { (w, s) ⇒
    CsvWriter(JacksonCsv.write(w, f(s))) { (out, ss) ⇒
      out.write(ss.toArray)
      ()
    }(_.close())
  }

  /** Default jackson.csv [[WriterEngine]].
    *
    * It's possible to tweak the behaviour of the underlying writer through [[writerEngineFrom]].
    */
  implicit val jacksonCsvWriterEngine: WriterEngine = writerEngineFrom(s ⇒ JacksonCsv.defaultWriterSchema(s))
}
