-- Enable the vector extension if not already enabled
CREATE EXTENSION IF NOT EXISTS vector;

-- Create files table
CREATE TABLE IF NOT EXISTS public.files (
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    checksum character varying(64) COLLATE pg_catalog."default",
    entry_created_at timestamp(6) with time zone,
    file_creation_time timestamp(6) without time zone,
    file_extension character varying(255) COLLATE pg_catalog."default",
    file_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    file_path character varying(255) COLLATE pg_catalog."default",
    file_size bigint,
    group_name character varying(100) COLLATE pg_catalog."default",
    is_hidden boolean,
    is_readonly boolean,
    is_sensitive boolean DEFAULT false,
    last_indexed timestamp(6) without time zone,
    file_last_modified timestamp(6) without time zone,
    mime_type character varying(255) COLLATE pg_catalog."default",
    owner character varying(100) COLLATE pg_catalog."default",
    permissions character varying(10) COLLATE pg_catalog."default",
    sensitive_reason jsonb,
    entry_modified_at timestamp(6) with time zone,
    CONSTRAINT files_pkey PRIMARY KEY (id)
);

-- Create file_chunks table
CREATE TABLE IF NOT EXISTS public.file_chunks (
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    chunk_index integer,
    content text COLLATE pg_catalog."default",
    created_at timestamp(6) with time zone,
    embedding vector(1024),
    file_extension character varying(255) COLLATE pg_catalog."default",
    file_name character varying(255) COLLATE pg_catalog."default",
    file_path character varying(255) COLLATE pg_catalog."default",
    last_indexed timestamp(6) without time zone,
    metadata jsonb,
    modified_at timestamp(6) with time zone,
    file_id character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT file_chunks_pkey PRIMARY KEY (id),
    CONSTRAINT fkhlh025a1p08hnfjhx16nvuvmn FOREIGN KEY (file_id)
        REFERENCES public.files (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);