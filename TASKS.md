basic comment renderer template (file) to be used in achievements and likes
improve notifications (link to referred resource)

use reactjs to improve rendering speed 
-> http://www.mono-software.com/blog/post/Mono/242/Improving-AngularJS-long-list-rendering-performance-using-ReactJS/
angular optimizations 
-> http://www.binpress.com/tutorial/speeding-up-angular-js-with-simple-optimizations/135
scroll forever
-> http://jsfiddle.net/vojtajina/U7Bz9/

sys: docker, nginx, snort, nagios, elastic search
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
