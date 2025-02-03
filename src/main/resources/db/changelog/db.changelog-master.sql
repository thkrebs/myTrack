
create table imei
(
    id          bigint generated by default as identity
        constraint pk_imei
            primary key,
    imei        varchar(255),
    phonenumber varchar(255),
    active      boolean not null,
    validfrom   timestamp(6),
    validto     timestamp(6)
);

create table position
(
    id         bigint generated by default as identity
                constraint pk_position
                primary key,
    altitude   smallint not null,
    angle      smallint not null,
    satellites smallint not null,
    speed      smallint not null,
    imei       varchar(255),
    datetime   timestamp(6),
    point      geometry(POINT,4326) not null
);


INSERT INTO imei (id, imei, active) VALUES (1, '866069064483413', TRUE);
INSERT INTO imei (id, imei, active) VALUES (2, '352016706223907', TRUE);

CREATE INDEX position_date_idx ON position (datetime,imei)  WITH (deduplicate_items = off);
CREATE INDEX parkspot_geom_idx ON parkspot USING GIST (point);
CREATE INDEX position_geom_idx ON position USING GIST (point);

