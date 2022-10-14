/*
 * Copyright 2022 HM Revenue & Customs
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

package unit.xml

import uk.gov.hmrc.customs.api.common.xml.PrettyPrinter
import util.UnitSpec

class PrettyPrinterSpec extends UnitSpec {

  "PrettyPrinter" should {
    "format xml with spaces inside elements" in {

      val prettyXml = PrettyPrinter.formatXml(<PreviousDocument><CategoryCode>Z</CategoryCode><ID>ABCDCDS03111026        02</ID><TypeCode>MCR</TypeCode></PreviousDocument>)
      prettyXml shouldBe
        """<PreviousDocument>
          |  <CategoryCode>Z</CategoryCode>
          |  <ID>ABCDCDS03111026        02</ID>
          |  <TypeCode>MCR</TypeCode>
          |</PreviousDocument>
          |""".stripMargin
    }

    "format xml with spaces inside attributes" in {

      val prettyXml = PrettyPrinter.formatXml(<PreviousDocument abc="123 A  B  C"><CategoryCode>Z</CategoryCode><ID>ABCDCDS03111026        02</ID><TypeCode>MCR</TypeCode></PreviousDocument>)
      prettyXml shouldBe
        """<PreviousDocument abc="123 A  B  C">
          |  <CategoryCode>Z</CategoryCode>
          |  <ID>ABCDCDS03111026        02</ID>
          |  <TypeCode>MCR</TypeCode>
          |</PreviousDocument>
          |""".stripMargin
    }

    "format xml with namespaces correctly" in {

      val prettyXml = PrettyPrinter.formatXml(<md:MetaData xmlns:md="urn:wco:datamodel:WCO:DocumentMetaData-DMS:2" xmlns="urn:wco:datamodel:WCO:DEC-DMS:2" xmlns:clm63055="urn:un:unece:uncefact:codelist:standard:UNECE:AgencyIdentificationCode:D12B" xmlns:ds="urn:wco:datamodel:WCO:MetaData_DS-DMS:2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="urn:wco:datamodel:WCO:DocumentMetaData-DMS:2 ../DocumentMetaData_2_DMS.xsd "><md:WCODataModelVersionCode>3.6</md:WCODataModelVersionCode> <md:WCOTypeName>DEC</md:WCOTypeName> <md:ResponsibleCountryCode>GB</md:ResponsibleCountryCode><md:ResponsibleAgencyName>HMRC</md:ResponsibleAgencyName> <md:AgencyAssignedCustomizationVersionCode>v2.1</md:AgencyAssignedCustomizationVersionCode> <Declaration xmlns:clm5ISO42173A="urn:un:unece:uncefact:codelist:standard:ISO:ISO3AlphaCurrencyCode:2012-08-31" xmlns:p1="urn:wco:datamodel:WCO:Declaration_DS:DMS:2" xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:6" xsi:schemaLocation="urn:wco:datamodel:WCO:DEC-DMS:2 ../WCO_DEC_2_DMS.xsd "><FunctionCode>9</FunctionCode></Declaration></md:MetaData>)
      prettyXml shouldBe
        """<md:MetaData xmlns="urn:wco:datamodel:WCO:DEC-DMS:2"
          |  xmlns:clm63055="urn:un:unece:uncefact:codelist:standard:UNECE:AgencyIdentificationCode:D12B"
          |  xmlns:ds="urn:wco:datamodel:WCO:MetaData_DS-DMS:2" xmlns:md="urn:wco:datamodel:WCO:DocumentMetaData-DMS:2"
          |  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="urn:wco:datamodel:WCO:DocumentMetaData-DMS:2 ../DocumentMetaData_2_DMS.xsd ">
          |  <md:WCODataModelVersionCode>3.6</md:WCODataModelVersionCode>
          |  <md:WCOTypeName>DEC</md:WCOTypeName>
          |  <md:ResponsibleCountryCode>GB</md:ResponsibleCountryCode>
          |  <md:ResponsibleAgencyName>HMRC</md:ResponsibleAgencyName>
          |  <md:AgencyAssignedCustomizationVersionCode>v2.1</md:AgencyAssignedCustomizationVersionCode>
          |  <Declaration xmlns:clm5ISO42173A="urn:un:unece:uncefact:codelist:standard:ISO:ISO3AlphaCurrencyCode:2012-08-31"
          |    xmlns:p1="urn:wco:datamodel:WCO:Declaration_DS:DMS:2"
          |    xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:6" xsi:schemaLocation="urn:wco:datamodel:WCO:DEC-DMS:2 ../WCO_DEC_2_DMS.xsd ">
          |    <FunctionCode>9</FunctionCode>
          |  </Declaration>
          |</md:MetaData>
          |""".stripMargin
    }

    "format dec info xml correctly" in {

      val prettyXml = PrettyPrinter.formatXml(<v2:queryDeclarationStatusResponse xmlns:v2="http://gov.uk/customs/declarationInformationRetrieval/status/v2" xmlns:urn="urn:wco:datamodel:WCO:Response_DS:DMS:2"
                                                                                 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                                                                 xmlns:dec="http://dmirs.core.ecf/DeclarationInformationRetrieval"
                                                                                 xmlns:urn1="urn:wco:datamodel:WCO:DEC-DMS:2"
                                                                                 xmlns:urn2="urn:wco:datamodel:WCO:Declaration_DS:DMS:2"
                                                                                 xmlns:fn="http://www.w3.org/2005/xpath-functions"
                                                                                 xmlns:xs="http://www.w3.org/2001/XMLSchema">
        <v2:responseCommon><v2:processingDate>2020-02-19T12:08:12.952Z</v2:processingDate></v2:responseCommon><v2:responseDetail><v2:retrieveDeclarationStatusResponse>
          <v2:retrieveDeclarationStatusDetailsList><v2:retrieveDeclarationStatusDetails><ns3:Declaration xmlns:ns3="http://gov.uk/customs/declarationInformationRetrieval/status/v2"
            xmlns="http://dmirs.core.ecf/DeclarationInformationRetrieval" xmlns:ns5="urn:wco:datamodel:WCO:DEC-DMS:2" xmlns:ns2="urn:wco:datamodel:WCO:Response_DS:DMS:2"
            xmlns:ns4="urn:wco:datamodel:WCO:Declaration_DS:DMS:2" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"><ns3:AcceptanceDateTime>
            <ns2:DateTimeString formatCode="304">20191010000000Z</ns2:DateTimeString></ns3:AcceptanceDateTime><ns3:ID>20GB1YQEOT8BCFGVR3</ns3:ID><ns3:VersionID>1</ns3:VersionID>
            <ns3:ReceivedDateTime><ns3:DateTimeString formatCode="304">20200219120306Z</ns3:DateTimeString></ns3:ReceivedDateTime><ns3:ICS>22</ns3:ICS></ns3:Declaration>
            <ns5:Declaration xmlns:ns5="urn:wco:datamodel:WCO:DEC-DMS:2" xmlns="http://dmirs.core.ecf/DeclarationInformationRetrieval" xmlns:ns2="urn:wco:datamodel:WCO:Response_DS:DMS:2"
                             xmlns:ns4="urn:wco:datamodel:WCO:Declaration_DS:DMS:2" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns3="http://gov.uk/customs/declarationInformationRetrieval/status/v2">
              <ns5:FunctionCode>9</ns5:FunctionCode><ns5:TypeCode>IMZ</ns5:TypeCode><ns5:GoodsItemQuantity>1</ns5:GoodsItemQuantity><ns5:TotalPackageQuantity>1.0</ns5:TotalPackageQuantity>
              <ns5:Submitter><ns5:ID>GB025115166435</ns5:ID></ns5:Submitter><ns5:GoodsShipment><ns5:PreviousDocument><ns5:ID>8GB830617936000-0110182</ns5:ID><ns5:TypeCode>DCR</ns5:TypeCode>
            </ns5:PreviousDocument><ns5:UCR><ns5:TraderAssignedReferenceID>9GB010969918000-0110182</ns5:TraderAssignedReferenceID></ns5:UCR></ns5:GoodsShipment></ns5:Declaration>
          </v2:retrieveDeclarationStatusDetails></v2:retrieveDeclarationStatusDetailsList></v2:retrieveDeclarationStatusResponse></v2:responseDetail></v2:queryDeclarationStatusResponse>)
      prettyXml shouldBe """<v2:queryDeclarationStatusResponse xmlns:dec="http://dmirs.core.ecf/DeclarationInformationRetrieval"
                           |  xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                           |  xmlns:urn="urn:wco:datamodel:WCO:Response_DS:DMS:2" xmlns:urn1="urn:wco:datamodel:WCO:DEC-DMS:2"
                           |  xmlns:urn2="urn:wco:datamodel:WCO:Declaration_DS:DMS:2"
                           |  xmlns:v2="http://gov.uk/customs/declarationInformationRetrieval/status/v2" xmlns:xs="http://www.w3.org/2001/XMLSchema">
                           |  <v2:responseCommon>
                           |    <v2:processingDate>2020-02-19T12:08:12.952Z</v2:processingDate>
                           |  </v2:responseCommon>
                           |  <v2:responseDetail>
                           |    <v2:retrieveDeclarationStatusResponse>
                           |      <v2:retrieveDeclarationStatusDetailsList>
                           |        <v2:retrieveDeclarationStatusDetails>
                           |          <ns3:Declaration xmlns="http://dmirs.core.ecf/DeclarationInformationRetrieval"
                           |            xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
                           |            xmlns:ns2="urn:wco:datamodel:WCO:Response_DS:DMS:2"
                           |            xmlns:ns3="http://gov.uk/customs/declarationInformationRetrieval/status/v2"
                           |            xmlns:ns4="urn:wco:datamodel:WCO:Declaration_DS:DMS:2" xmlns:ns5="urn:wco:datamodel:WCO:DEC-DMS:2">
                           |            <ns3:AcceptanceDateTime>
                           |              <ns2:DateTimeString formatCode="304">20191010000000Z</ns2:DateTimeString>
                           |            </ns3:AcceptanceDateTime>
                           |            <ns3:ID>20GB1YQEOT8BCFGVR3</ns3:ID>
                           |            <ns3:VersionID>1</ns3:VersionID>
                           |            <ns3:ReceivedDateTime>
                           |              <ns3:DateTimeString formatCode="304">20200219120306Z</ns3:DateTimeString>
                           |            </ns3:ReceivedDateTime>
                           |            <ns3:ICS>22</ns3:ICS>
                           |          </ns3:Declaration>
                           |          <ns5:Declaration xmlns="http://dmirs.core.ecf/DeclarationInformationRetrieval"
                           |            xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
                           |            xmlns:ns2="urn:wco:datamodel:WCO:Response_DS:DMS:2"
                           |            xmlns:ns3="http://gov.uk/customs/declarationInformationRetrieval/status/v2"
                           |            xmlns:ns4="urn:wco:datamodel:WCO:Declaration_DS:DMS:2" xmlns:ns5="urn:wco:datamodel:WCO:DEC-DMS:2">
                           |            <ns5:FunctionCode>9</ns5:FunctionCode>
                           |            <ns5:TypeCode>IMZ</ns5:TypeCode>
                           |            <ns5:GoodsItemQuantity>1</ns5:GoodsItemQuantity>
                           |            <ns5:TotalPackageQuantity>1.0</ns5:TotalPackageQuantity>
                           |            <ns5:Submitter>
                           |              <ns5:ID>GB025115166435</ns5:ID>
                           |            </ns5:Submitter>
                           |            <ns5:GoodsShipment>
                           |              <ns5:PreviousDocument>
                           |                <ns5:ID>8GB830617936000-0110182</ns5:ID>
                           |                <ns5:TypeCode>DCR</ns5:TypeCode>
                           |              </ns5:PreviousDocument>
                           |              <ns5:UCR>
                           |                <ns5:TraderAssignedReferenceID>9GB010969918000-0110182</ns5:TraderAssignedReferenceID>
                           |              </ns5:UCR>
                           |            </ns5:GoodsShipment>
                           |          </ns5:Declaration>
                           |        </v2:retrieveDeclarationStatusDetails>
                           |      </v2:retrieveDeclarationStatusDetailsList>
                           |    </v2:retrieveDeclarationStatusResponse>
                           |  </v2:responseDetail>
                           |</v2:queryDeclarationStatusResponse>
                           |""".stripMargin
    }
  }
}


