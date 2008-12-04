CREATE TABLE DEPLOYMENT (ID bigint not null, URL varchar(512) not null, FILENAME varchar(512) not null, DEPLOY_DATE timestamp not null, primary key (ID))
CREATE TABLE RESOURCE (ID bigint not null, URL varchar(512) not null, CRC varchar(128), FILENAME varchar(512) not null, primary key (ID))
CREATE TABLE PENDING (DEPLOYMENT_ID bigint not null, RESOURCE_ID bigint not null)
ALTER TABLE PENDING ADD CONSTRAINT FK_PENDING_DEPLOYMENT FOREIGN KEY (DEPLOYMENT_ID) REFERENCES DEPLOYMENT;
ALTER TABLE PENDING ADD CONSTRAINT FK_PENDING_RESOURCE FOREIGN KEY (RESOURCE_ID) REFERENCES RESOURCE;