discuss/moderate/monitor/notifications/browse
vote/tag loeschen
pageable for all lists

- Done: "No way to get in touch" http://www.smashingmagazine.com/2009/02/18/9-common-usability-blunders/
- Live stats: 93 users viewing
- Moderate Thread (with Reports, Pending)
- Details of Thread with Stats e.g. http://www.mixcloud.com/stats/
- Monitoring for Superuser/Mods?, Fraud warnings via mail
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
- Elastic Search
- All links with <domain>/out to get stats
- Notifications (websocket?) for replied, mentioned, upvoted
- Clean old Votes, Notices
- Audit logs
- Comment
    - Mod can pin/sticky a comment to be first in list
    - @name messages
    - #hashtags in comment
    - quotations
    - allow embeds, images
- Clean: remove tracker (jhipster corps), currently typing websockets
- Async, maybe event driven via akka
    @Scheduled
    - Thread stats like commentCount/likes/dislikes
    - Reputation
- Anomaly detection for
    - users: account hijacking
    - comment flooding
