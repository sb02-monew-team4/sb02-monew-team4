create table articles
(
    id             uuid                        not null primary key,
    comment_count  bigint                      not null,
    created_at     timestamp(6) with time zone not null,
    is_deleted     boolean                     not null,
    original_link  text                        not null
        constraint uk_article_original_link unique,
    published_date timestamp(6) with time zone not null,
    source         varchar(20)                 not null,
    summary        varchar(150)                not null,
    title          varchar(100)                not null,
    view_count     bigint                      not null
);

alter table articles
    owner to postgres;

create table interests
(
    id               uuid                        not null primary key,
    created_at       timestamp(6) with time zone not null,
    name             varchar(100)                not null
        constraint uk_interests_name unique,
    subscriber_count bigint                      not null,
    updated_at       timestamp(6) with time zone not null
);

alter table interests
    owner to postgres;

create table article_interests
(
    article_id  uuid not null
        constraint fk_article_interests_article references article (id) on delete cascade,
    interest_id uuid not null
        constraint fk_article_interests_interest references interests (id) on delete cascade,
    primary key (article_id, interest_id)
);

alter table article_interests
    owner to postgres;

create table interest_keywords
(
    id          uuid         not null primary key,
    keyword     varchar(100) not null,
    interest_id uuid         not null
        constraint fk_interest_keywords_interest references interests (id) on delete cascade
);

alter table interest_keywords
    owner to postgres;

create table users
(
    id         uuid                        not null primary key,
    created_at timestamp(6) with time zone not null,
    email      varchar(254)                not null
        constraint uk_users_email unique,
    is_deleted boolean                     not null,
    nickname   varchar(50)                 not null,
    password   varchar(100)                not null
);

alter table users
    owner to postgres;

create table article_views
(
    id         uuid                        not null primary key,
    viewed_at  timestamp(6) with time zone not null,
    article_id uuid                        not null
        constraint fk_article_views_article references article (id) on delete cascade,
    user_id    uuid                        not null
        constraint fk_article_views_user references users (id) on delete cascade,
    constraint uq_article_views_article_user unique (article_id, user_id)
);

alter table article_views
    owner to postgres;

create table comments
(
    id         uuid                        not null primary key,
    contents   text                        not null,
    created_at timestamp(6) with time zone not null,
    is_deleted boolean,
    like_count bigint,
    updated_at timestamp(6) with time zone not null,
    article_id uuid                        not null
        constraint fk_comments_article references article (id),
    user_id    uuid                        not null
        constraint fk_comments_user references users (id)
);

alter table comments
    owner to postgres;

create table comment_likes
(
    id         uuid                        not null primary key,
    created_at timestamp(6) with time zone not null,
    comment_id uuid                        not null
        constraint fk_comment_likes_comment references comments (id) on delete cascade,
    user_id    uuid                        not null
        constraint fk_comment_likes_user references users (id) on delete cascade
);

alter table comment_likes
    owner to postgres;

create table notifications
(
    id            uuid                        not null primary key,
    confirmed     boolean                     not null,
    content       varchar(255)                not null,
    created_at    timestamp(6) with time zone not null,
    resource_id   uuid                        not null,
    resource_type varchar(255)                not null
        constraint chk_notifications_resource_type check (
            (resource_type)::text = ANY (
                (ARRAY ['INTEREST'::varchar, 'COMMENT'::varchar])::text[]
                )
            ),
    updated_at    timestamp(6) with time zone not null,
    user_id       uuid
        constraint fk_notifications_user references users (id)
);

alter table notifications
    owner to postgres;

create table subscriptions
(
    id          uuid                        not null primary key,
    created_at  timestamp(6) with time zone not null,
    interest_id uuid                        not null
        constraint fk_subscriptions_interest references interests (id) on delete cascade,
    user_id     uuid                        not null
        constraint fk_subscriptions_user references users (id) on delete cascade
);

alter table subscriptions
    owner to postgres;
