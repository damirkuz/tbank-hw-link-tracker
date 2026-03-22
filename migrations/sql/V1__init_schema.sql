create table if not exists chats (
                                     chat_id bigint primary key
);

create table if not exists links (
                                     id bigint generated always as identity primary key,
                                     uri varchar(2048) not null,
                                     tracked_resource varchar(100) not null,
                                     last_update timestamp with time zone,
                                     constraint uq_links_uri_resource unique (uri, tracked_resource)
);

create table if not exists subscriptions (
                                             id bigint generated always as identity primary key,
                                             chat_id bigint not null,
                                             link_id bigint not null,
                                             constraint fk_subscriptions_chat
                                                 foreign key (chat_id) references chats (chat_id) on delete cascade,
                                             constraint fk_subscriptions_link
                                                 foreign key (link_id) references links (id) on delete cascade,
                                             constraint uq_subscriptions_chat_link unique (chat_id, link_id)
);

create table if not exists subscription_tags (
                                                 subscription_id bigint not null,
                                                 tag varchar(255) not null,
                                                 constraint fk_subscription_tags_subscription
                                                     foreign key (subscription_id) references subscriptions (id) on delete cascade,
                                                 constraint uq_subscription_tags unique (subscription_id, tag)
);

create table if not exists subscription_filters (
                                                    subscription_id bigint not null,
                                                    filter_value varchar(255) not null,
                                                    constraint fk_subscription_filters_subscription
                                                        foreign key (subscription_id) references subscriptions (id) on delete cascade,
                                                    constraint uq_subscription_filters unique (subscription_id, filter_value)
);
