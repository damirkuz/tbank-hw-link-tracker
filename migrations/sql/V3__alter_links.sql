alter table links
    add column if not exists next_check_at timestamp with time zone;

update links
set next_check_at = now()
where next_check_at is null;

create index if not exists idx_links_next_check_at_id
    on links(next_check_at, id);
