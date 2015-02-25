Information Retrieval for Visualzations
=======================================

This is a scrapy based scraper to harvest comment websites for visualizations.


Requirements
------------
- python 2.7
- scrapy


Spiders
------
tiwag: phorum on tiwag.org


Installation
------------

	sudo apt-get install python-pip
	sudo pip2 install scrapy


Getting Started
---------------
Replace *<spider>* with the name of desired spider implementation. Note that this is not the name of the file, but the attribute *name* in the implementation. All spider lie in folder *harvester/spiders/*.


	scrapy crawl <spider> -o scraped_data.json


