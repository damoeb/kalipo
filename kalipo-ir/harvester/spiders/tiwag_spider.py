import scrapy
from scrapy.contrib.spiders import CrawlSpider, Rule
from scrapy.contrib.linkextractors import LinkExtractor
from scrapy.selector import Selector

from harvester.items import Comment
import time
import calendar

class TiwagSpider(CrawlSpider):
    name = "tiwag"
    allowed_domains = ["dietiwag.org"]
    start_urls = [
	"http://www.dietiwag.org/phorum_2/list.php?f=2",
	"http://www.dietiwag.org/phorum_2/read.php?f=2&i=120470&t=120470"
    ]

    rules = (
        Rule(LinkExtractor(allow=('list.php'))),
        Rule(LinkExtractor(allow=('read\.php', )), callback='parse_item'),
    )

    def clean_str(self, val):
	return val.replace(u'\xa0', u'').replace('\n','').strip()
    
    def parse_item(self, response):
        sel = Selector(response)
        comments = sel.xpath('//font[@class="PhorumMessage"]')
        items = []
        for one in comments:
	    item = Comment()
	    item['url'] = response.url
	    texts = one.xpath('text()').extract()
	    item['author'] = self.clean_str(texts[0]).replace('Autor:','')
	    item['text'] = self.clean_str(' '.join(texts[2:len(texts)]))
	    try:
		item['date'] = calendar.timegm(time.strptime(self.clean_str(texts[1]).replace('Datum:',''), "%d-%m-%y %H:%M"))
	    except ValueError:
		print "Error: "+' '.join(texts)
		continue

	    items.append(item)
     
        return items

