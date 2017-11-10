-- noinspection SqlNoDataSourceInspectionForFile
set mode PostgreSQL;

create table Plans ( --Excel tour list
  id varchar(5),
  name varchar(50),
  shortDescription varchar(200),
  length int,
  departure varchar(50), --Mon, Tue, 20171001
  price float,
  weekendPrice float, --set to 0 if no weekend departure
  primary key(id)
);

create table Tours ( --Excel booking list
  planId varchar(5),
  tourDate date,
  guideName varchar(16),
  guideAccount varchar(16),
  hotel varchar(16),
  capacity int,
  minimum int,
  primary key (planId, tourDate)
);

create table Customers (
  id varchar(33),
  name varchar(32),
  gender varchar(1),
  age int,
  phoneNumber varchar(16),
  state varchar(16),
  primary key (id)
);

create table Dialogues (
  customerId varchar(33),
  sendTime timestamp,
  content varchar(500),
  primary key (customerId, sendTime)
);

create table FAQ (
  question varchar(128),
  answer varchar(512),
  primary key (question)
);

create table Tags (
  name varchar(128),
  customerId varchar(33),
  primary key(name)
);

create table Bookings (
  customerId varchar(33),
  planId varchar(5),
  tourDate date,
  adults int,
  children int,
  toddlers int,
  fee float,
  paid float,
  specialRequest varchar(128),
  primary key (customerId, planId, tourDate)
);
