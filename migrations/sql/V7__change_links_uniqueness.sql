alter table links
    drop constraint if exists uq_links_uri_resource;

alter table links
    add constraint uq_links_uri unique (uri);
