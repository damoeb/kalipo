configuration attr
- discussion depth: NUM {min:2, default:4}
- max comments: NUM {min:0}
- max comment length: NUM {min:160, max:2048}
- keep ips: BOOL {default:false}
- support markdown: BOOL {default:false}
- support show-more: BOOL {default:true}
- max-visible-lines-of-comment: NUM {min:1, default:5}
- browse: thread order: best score first, last changed first
- notifications of super-mods via mail too (reports): BOOL {default: true}

Suche!!!
show-more directive

UI: adapt view using a filter: Auto/Pending/None
oneline: es kann auf die gesamte zeile geklickt werden, nicht nur das Icon rechts, um es komplett anzuzeigen

highlight mod comments

raw rendered usergen texts in html e.g. showReplyModal

repliescount is mixed in hidden and dropped

--

improve notifications (link to referred resource)

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

- Anomaly detection for
    - users: account hijacking
    - comment flooding
