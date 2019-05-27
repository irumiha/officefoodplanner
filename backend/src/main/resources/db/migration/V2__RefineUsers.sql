alter table auth.users drop column phone;
alter table auth.users rename hash to password_hash;
alter table auth.users add column active boolean not null default true;
