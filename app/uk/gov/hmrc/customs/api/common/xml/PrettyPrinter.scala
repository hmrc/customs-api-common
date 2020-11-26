/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.customs.api.common.xml

import java.io.{StringReader, StringWriter}

import com.sun.org.apache.xml.internal.serialize.{OutputFormat, XMLSerializer}
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource

import scala.xml.NodeSeq

object PrettyPrinter {

  private val lineWidth = 120
  private val indent = 2

  def formatXml(xml: NodeSeq): String = {
    lazy val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = db.parse(new InputSource(new StringReader(xml.toString())))
    val format = new OutputFormat(doc)
    format.setIndenting(true)
    format.setIndent(indent)
    format.setLineWidth(lineWidth)
    format.setOmitXMLDeclaration(true)
    val outxml = new StringWriter()
    val serializer = new XMLSerializer(outxml, format)
    serializer.serialize(doc)
    outxml.toString
  }
}

