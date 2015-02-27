import json
import argparse

parser = argparse.ArgumentParser(description='Convert a scrapy comment dump to kalipo import xml.')
parser.add_argument('-f','--file', help='scrapy json argument', required=True)

args = parser.parse_args()

json_data=open(args.file)

data = json.load(json_data)

index=1

for c in data:
	'''
	db.T_COMMENT.insert({
            "_id" : ObjectId("5484cc19c8301dc174baf001"),
            "threadId" : "5484cc19c832000009108573",
            "body" : "http wer die Zuschauerzahlen dieser buli saison",
            "authorId" : "admin",
            "level" : 0,
            "fingerprint" : "99998",
            "displayName" : "GedriteAlAlMg-Si-",
            "status" : "APPROVED",
            "createdDate": new Date()
            });
	'''
	print "%02d" % (index,)	
	print c['thread']
	index += 1	

json_data.close()
