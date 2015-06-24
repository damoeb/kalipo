kalipo
==========================
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/damoeb/kalipo?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

What is it?
-----------

Kalipo is a realtime mobile-first commenting web application with a strong focus on transparency. It uses [d3js](http://d3js.org/) vizualizations to help users to navigate through a discussion and find the relevant parts and websockets to stay up to date without refreshing the page. Its not a classic social network, since user can not network. 

> Its the statement that matters, not the person who said it!

Features
--------
- Pseudonymity
- Reputation: We try to minimize the need for moderation by implementing a
strong reputation based privilege system, which you may know from stackoverflow.
<!--
- Webhooks: Hook extern urls to a specific thread, that will forward you to the particular thread
e.g. assume *www.example.com/2000008228049* is hooked by thread 1, then a user on that external page can simply use our domain as prefix *kalipo-url.com/http://www.example.com/2000008228049* to get redirected to the thread 1.
-->
- Livecycle: track your comment livecycle all the time
- Notifications: we will inform you, when someone likes you're comment, you got a reply,...
- Moderation: mods can choose from a range of actions to keep discussion sane and vital
- Sanity: Apply best practices for well known threats like sock puppets, trolls, spamming (see https://github.com/damoeb/kalipo/wiki/Frauds)

The Latest Version
------------------
There is a [running prototype](https://176.28.19.89/kalipo/). Be gentle!

Screenshot
----------

![kalipo screenshot](https://raw.githubusercontent.com/damoeb/kalipo/master/doc/screenshots/discussion.png)

Documentation
------------
There is a rudimentary [docs file]{docs.html}, which I am still writing on. Otherwise, stick to the code.


Installation
------------
### Requirements
* git
* java 1.8
* mongo db

### Getting Started

    git clone https://github.com/damoeb/kalipo.git
    ./gradlew build -info
    ./gradlew run

This will start an embedded tomcat on http://localhost:8080 (as defined in kalipo-service/src/main/resources/config/application-[prod|dev].yml)

### Modules

* *kalipo-client* the angularjs based frontend
* *kalipo-Service* the java/spring based backend


Configuration
-------------

Will follow.

Licensing
---------

Please see the file called LICENSE.

Contacts
--------
@damoeb
