create table if not exists tags (
                                    id bigserial primary key,
                                    chat_id bigint not null references chats(chat_id) on delete cascade,
                                    name varchar(255) not null,
                                    unique (chat_id, name)
);

create table if not exists filters (
                                       id bigserial primary key,
                                       chat_id bigint not null references chats(chat_id) on delete cascade,
                                       value varchar(255) not null,
                                       unique (chat_id, value)
);

create table if not exists subscription_tag_links (
                                                      id bigserial primary key,
                                                      subscription_id bigint not null references subscriptions(id) on delete cascade,
                                                      tag_id bigint not null references tags(id) on delete cascade,
                                                      unique (subscription_id, tag_id)
);

create table if not exists subscription_filter_links (
                                                         id bigserial primary key,
                                                         subscription_id bigint not null references subscriptions(id) on delete cascade,
                                                         filter_id bigint not null references filters(id) on delete cascade,
                                                         unique (subscription_id, filter_id)
);

-- Перенос старых string-tag и string-filter в отдельные сущности
insert into tags(chat_id, name)
select distinct s.chat_id, st.tag
from subscription_tags st
         join subscriptions s on s.id = st.subscription_id
on conflict (chat_id, name) do nothing;

insert into filters(chat_id, value)
select distinct s.chat_id, sf.filter_value
from subscription_filters sf
         join subscriptions s on s.id = sf.subscription_id
on conflict (chat_id, value) do nothing;

insert into subscription_tag_links(subscription_id, tag_id)
select distinct st.subscription_id, t.id
from subscription_tags st
         join subscriptions s on s.id = st.subscription_id
         join tags t on t.chat_id = s.chat_id and t.name = st.tag
on conflict (subscription_id, tag_id) do nothing;

insert into subscription_filter_links(subscription_id, filter_id)
select distinct sf.subscription_id, f.id
from subscription_filters sf
         join subscriptions s on s.id = sf.subscription_id
         join filters f on f.chat_id = s.chat_id and f.value = sf.filter_value
on conflict (subscription_id, filter_id) do nothing;

drop table if exists subscription_tags;
drop table if exists subscription_filters;
