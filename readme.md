
What is  it?
============

This is a java project implementing a REST Data server with OAuth2 security and GIT operations support.


Overview
===========
This service architected as transient layer between any Git repository and Salesforce.
The Spring Framework abstraction is chosen as skeleton for all project, OAuth2 access implemented via Spring Security Framework.
Access to remote git repository implemented via JGit library.



System Requirements 
=====================
In order to  compile and deploy this web application the following software is needed.

* Maven 3.x

* Java JDK 1.8

* Heroku CLI

For starting a server:

* Java JRE 1.8




Installing server
========================

To install on Heroku, hit the following commands:

heroku create

git init
git add -A
git commit -m "ready to deploy"
git remote add heroku https://git.heroku.com/<app-name>.git (replace <app-name> with your own heroku  application's name)
git push heroku master

The  base access point has the form https://<app-name>.herokuapp.com

Set credentials to access the service (the same must be set on "Git Settings" tab in Flosum):

heroku config:set username=testclient
heroku config:set password=password


(Config vars can also be  set up  through Heroku's web interface - on your apps settings tab on Dashboard)


