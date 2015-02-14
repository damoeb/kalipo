raw rendered usergen texts in html e.g. showReplyModal

unlimited scroll: http://engineering.linkedin.com/linkedin-ipad-5-techniques-smooth-infinite-scrolling-html5

repliescount is mixed in hidden and dropped
support images in comments
support links in comment
gatling/jmeter for load tests

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

improve notifications (link to referred resource)

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

- Anomaly detection for
    - users: account hijacking
    - comment flooding
