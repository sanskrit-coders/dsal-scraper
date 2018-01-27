package dsal

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.json4s.DefaultFormats
import org.json4s.native.Serialization
import org.scalatest.{Assertion, FlatSpec}
import org.slf4j.LoggerFactory
import sanskritCoders.dsal.DsalPLinkedPageDict
import sanskritCoders.dsal.items.DsalDictItem

import scala.io.Source

case class DictPageTestCase(url: String, numItems: Int, sampleItem: DsalDictItem)
case class DictPageListTestCase(pageLength: Int, samplePage: DictPageTestCase)

class DsalPLinkedPageDictTest  extends FlatSpec {
  private val log = LoggerFactory.getLogger(this.getClass)
  implicit val formats: DefaultFormats.type = DefaultFormats
  val browser: JsoupBrowser = JsoupBrowser.typed()

  def testPageListParse(name: String, testSpec : DictPageListTestCase): Unit = {
    val dict = new DsalPLinkedPageDict(name = name, browser = browser)
    val pages = dict.getPages()
    assert(pages.length == testSpec.pageLength)
    val items = dict.getItems(pageUrl = testSpec.samplePage.url)
    assert(items.length == testSpec.samplePage.numItems)
    val sampleItem = testSpec.samplePage.sampleItem
    val matchingItem = items.filter(_.headwords.contains(sampleItem.headwords.toList.head)).head
    assert(matchingItem.toString() == sampleItem.toString)
  }

  "DsalPLinkedPageIterator" should "list pages correctly" in {
    val source = Source.fromResource("DictPageParseTests.json")
    val tests = Serialization.read[Map[String, DictPageListTestCase]](source.mkString)
    log.debug(tests.toString)
    tests.foreach{
      case (name: String, testCase: DictPageListTestCase) => testPageListParse(name=name, testSpec = testCase)
    }
  }
}
