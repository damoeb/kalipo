raw rendered usergen texts in html e.g. showReplyModal

repliescount is mixed in hidden and dropped
comment data is messy and contains duplicate ids
infinite scroll
support images in comments
support links in comment
gatling/jmeter for load tests

score: include freshness for roots if younger than 3 days

dropped
- score <= 0 comments, older than n views
- replies.position() > 5

oneline
- hide.length <= 2

verbose
- comment with replies

comment relevance: {
    duration: {
        from: createdAt
        to: last upvote
    },
    attraction: {
        replies,
        upvotes/downvotes
    }
}

normalize
- see todos
- upvotes/downvotes max 10 per comment
- scores


Use @Cacheable with ehcache see http://viralpatel.net/blogs/cache-support-spring-3-1-m1/


Visualization
=============

http://blog.revolutionanalytics.com/2009/09/hierarchical-clustering-in-r.html
http://silenced.co/wp-content/uploads/2014/10/religions_tree.jpg
http://bocoup.com/img/weblog/career-tree-obama.png
http://visualizing.org/sites/default/files/imagecache/embedded_vis_medium/images/0023.png

Colors:
    black: subthread
    gray-blue: replies
    gray-orange: mod-comment
    gray-red: sticky-comment


--

at first, render just a stub. create a directive that will render the full comment when visible 

basic comment renderer template (file) to be used in achievements and likes
improve notifications (link to referred resource)

sys: [docker, snort, nagios], elastic search
- Achievements: stackoverflow zeigt die die rep upgrades an wie soundcloud die notifications 

discuss/moderate/monitor/notifications/browse
pageable for all lists

- Live stats: 93 users viewing
- Details of Thread with Stats e.g. http://www.mixcloud.com/stats/
- Monitoring for Superuser, Fraud warnings via mail
    - mods: approvals/deletions/k-listing, (automated) thread deletions
    - users: live activity, reports, critical activity -> lock
    - review popular comments/links
    - system: spam detection, resource consumption
    http://www.poweradmin.com/blog/server-monitoring-best-practices/

    charts
    http://cdn.soasta.com/wp/wp-content/uploads/2013/06/SOASTA-mPulse-WhatIf-Analysis1.png
    https://segment.com/blog/announcing-pingdom-real-user-monitoring-integration/images/pingdom-rum-long-tail.png

    - anomaly detection
    - def metrics
- All links with <domain>/out to get stats

- Audit logs
- Comment
    - Mod can pin/sticky a comment to be first in list
    - @name messages
    - #hashtags in comment
    - quotations
    - allow embeds, images
- Clean: currently typing websockets
- Async, maybe event driven via akka
    @Scheduled
    - Thread stats like commentCount/likes/dislikes
    - Reputation
- Anomaly detection for
    - users: account hijacking
    - comment flooding
