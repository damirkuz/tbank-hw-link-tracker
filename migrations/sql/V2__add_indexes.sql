create index if not exists idx_subscriptions_chat_id
    on subscriptions(chat_id);

create index if not exists idx_subscriptions_link_id
    on subscriptions(link_id);

create index if not exists idx_links_last_update_id
    on links(last_update asc, id asc);
