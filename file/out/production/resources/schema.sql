create table if not exists Note (
 id int AUTO_INCREMENT  PRIMARY KEY,
 path varchar(50) not null,
 note varchar(100) not null
);