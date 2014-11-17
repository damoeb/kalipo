kalipo
==========================

What is it?
-----------

This a prototype for a realtime commenting web application with a strong focus on transparency.

Features
--------
- Reputation: We try to minimize the need for moderation by implementing a
strong reputation based privilege system, which you may know from stackoverflow.
- Webhooks: Hook extern urls to a specific thread, that will forward you to the particular thread
e.g. assume http://derstandard.at/2000008228049 is hooked by thread 1, then a user on that external page can simply use our domain as prefix kalipo.org/http://derstandard.at/2000008228049 to get redirected to the thread 1.
- Transparency: track your comment livecycle all the time
- Notifications: many transient actions trigger notifications to keep you informed
- Moderation: mods can choose from a range of actions (similar to the IRC k-List, g-List or z-Line) keep discussion same
- Ugly duckling: threads that do not attrack a number of commentors within 48h after creation will be deleted

The Latest Version
------------------
There is no final version yet.


Documentation
------------
There is no documentation yet, just the code.


Installation
------------

    git clone https://github.com/damoeb/kalipo.git
    ./gradlew run

### Requirements
* git
* java 1.8
* mongo db

### Modules
descriptions of all the project, and all sub-modules and libraries
* *kalipo-client* the angularjs based frontend
* *kalipo-Service* the java/spring based backend

instructions to install, configure, and to run the programs
5-line code snippet on how its used (if it's a library)


Licensing
------------

Please see the file called LICENSE.

Contacts
--------
@damoeb
