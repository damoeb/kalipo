import scrapy
from scrapy.contrib.spiders import CrawlSpider, Rule
from scrapy.contrib.linkextractors import LinkExtractor
from scrapy.selector import Selector

from harvester.items import Comment
import time
import calendar

class HeiseSpider(CrawlSpider):
    name = "heise"
    allowed_domains = ["www.heise.de"]
    start_urls = [
	#"http://www.heise.de/tp/forum",
	"http://www.heise.de/tp/foren/S-Syrien-150-Christen-von-IS-entfuehrt/forum-292707/list/"
    ]

    rules = (
        Rule(LinkExtractor(allow=('/tp/foren/[^/]+/forum-[0-9]+/list'))),
        Rule(LinkExtractor(allow=('read')), callback='parse_item')
    )

    def clean_str(self, val):
	return val.replace(u'\xa0', u'').replace('\n','').strip()
    
    def parse_item(self, response):
        sel = Selector(response)

	item = Comment()
	item['text'] = sel.xpath('//posting_subject/text()').extract() + sel.xpath('//posting_text/html()').extract()
	item['url'] = response.url
	item['author'] = sel.xpath('//user_info/i/text()').extract()
	item['date'] = 'none' #calendar.timegm(time.strptime(self.clean_str(texts[1]).replace('Datum:',''), "%d-%m-%y %H:%M"))
        return item

