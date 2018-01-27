package sanskritCoders.dsal

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import sanskritCoders.dsal.DsalPLinkedPageDict.{getClass, log}
import sanskritCoders.dsal.items.DsalDictItem

class DsalPLinkedPageDict(name: String, browser: JsoupBrowser) {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def getPages(): Seq[String] = {
    // Scrape links to entries, as in:
    //  http://dsalsrv02.uchicago.edu/cgi-bin/philologic/search3advanced?dbname=burrow&searchdomain=headwords&display=utf8

    val indexPage = s"http://dsalsrv02.uchicago.edu/cgi-bin/philologic/search3advanced?dbname=$name&searchdomain=headwords&display=utf8"

    val doc = browser.get(url = indexPage)
    log.info(s"Read $indexPage")
    val itemPageElements = doc.underlying.getElementsByAttributeValueContaining("href", "?p.").toArray().map(_.asInstanceOf[Element])
    val itemPageUrls = itemPageElements.map("http://dsalsrv02.uchicago.edu" + _.attr("href")).distinct
    val likelySize: Int = itemPageUrls.length
    log.info(s"Got about ${likelySize} items.")
    itemPageUrls
  }

  def getItems(pageUrl: String): Seq[DsalDictItem] = {
    val doc = browser.get(url = pageUrl)
    log.info(s"Read $pageUrl")
    val itemElements = doc.underlying.getElementsByAttributeValueContaining("type", "article").toArray().map(_.asInstanceOf[Element])
//     Strangely, for http://dsalsrv02.uchicago.edu/cgi-bin/philologic/getobject.pl?c.0:1.turner :
//    The above yields elements like:
    //<div2 type="article" id="अ_a">
//    <span class="head"><span class="hi">अ a</span></span>
//    </div2>
//    rather than:
//    <div2 type="article" id="अ_a">
//      <span class="head"><span class="hi">अ a</span></span>
//      <p></p><p>&amp;super2;, or <i>aḥ</i>, exclamation expressing (1) pity;  --  (2) disapprobation;  --  (3) indifference.</p>

    itemElements.map(element => {
      val item = new DsalDictItem()
      item.fromPageElement(element)
      item
    })
  }
}

object DsalPLinkedPageDict {
  private val log = LoggerFactory.getLogger(getClass.getName)
}