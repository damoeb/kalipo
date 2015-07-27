import scrapy
from scrapy.contrib.spiders import CrawlSpider, Rule
from scrapy.contrib.linkextractors import LinkExtractor
from scrapy.selector import Selector

from harvester.items import Comment
import time
import calendar
import re

'''
$('.thread_user').remove(); $('.thread_date').remove(); $('.thread_title > a').each(function(i,e) {var $e = $(e); $e.text(new Array($e.parents().length -10).join( ' ' ) + $e.attr('href'))})
$('.thread_votechart>a>img').each(function(i,e) {var $e = $(e); $e.parent().text($e.attr('alt').replace('%',''))});
$('.thread_votechart>img').each(function(i,e) {var $e = $(e); $e.parent().text('0')});
'''

class TpSpider(CrawlSpider):
    name = "telepolis"
    allowed_domains = ["www.heise.de"]
    start_urls = [
            "http://www.heise.de/tp/foren/S-MH17-US-Geheimdienste-sollen-endlich-liefern/forum-295328/list/"
    ]

    rules = (
        Rule(LinkExtractor(allow=('/forum-295328/list/.*'))),
	    Rule(LinkExtractor(allow=('/msg-[0-9]+/read')), callback='parse_item')
    )

    def clean_str(self, val):
	    return val.replace(u'\xa0', u' ').strip()

    def to_str(self, arr):
    	return self.clean_str(''.join(arr))

    def parse_date(self, val):
        grps = re.search('[0-9]+\. ([A-Za-z]+) [0-9]{4} [0-9]{2}:[0-9]{2}', val)

        mnth = grps.group(1)

        months = ['Januar', 'Februar', 'M\u00e4rz', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember']
        for index, item in enumerate(months):
           if item.lower() == mnth.lower():
              val = val.replace(mnth, str(index))
              break

        return calendar.timegm(time.strptime(val, "%d. %m %Y %H:%M"))

    def parse_item(self, response):
        sel = Selector(response)

	'''
	isRoot = len(response.xpath("//ul[@class='forum_navi'][2]/li")) == 6

	if !isRoot:
	   # find parent
	   parent = response.xpath("//span[@class='active_post']/../../../parent::ul[@class='nextlevel_line']/preceding-sibling::div[@class='hover_line']")
	   # get link
	   link = parent.xpath(".//div[@class='thread_title']/a")
	   # extract parent id from href
	'''

	item = Comment()
	item['text'] = self.to_str(sel.xpath("//h3[@class='posting_subject']/text()").extract()) + self.to_str(sel.xpath("//p[@class='posting_text']/text()").extract())
	item['url'] = response.url
	item['id'] = re.search('msg-[0-9]+', response.url).group(0)
	item['parent'] = 'unknown'
	item['level'] = 0
	#item['thread'] = re.search('forum-([0-9]+)', response.url).group(1)
	item['author'] = self.to_str(sel.xpath("//div[@class='user_info']/i/text()").extract())
	#item['date'] = self.parse_date(self.to_str(response.xpath("//div[@class='posting_date']/text()").extract()))
        return item

