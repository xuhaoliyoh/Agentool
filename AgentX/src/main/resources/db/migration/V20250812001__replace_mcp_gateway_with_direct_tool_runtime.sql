create table if not exists tool_runtime_configs (
    id varchar(36) primary key,
    tool_id varchar(36) not null,
    tool_version varchar(50),
    runtime_type varchar(50) not null,
    transport_type varchar(50),
    endpoint_url text,
    http_method varchar(20),
    auth_type varchar(50) not null default 'NONE',
    auth_config_schema jsonb,
    default_headers jsonb,
    request_schema jsonb,
    response_schema jsonb,
    tool_definitions jsonb,
    mcp_server_name varchar(255),
    timeout_ms integer not null default 30000,
    enabled boolean not null default true,
    created_at timestamp without time zone not null default current_timestamp,
    updated_at timestamp without time zone not null default current_timestamp,
    deleted_at timestamp without time zone
);

create index if not exists idx_tool_runtime_configs_tool_id on tool_runtime_configs(tool_id);
create index if not exists idx_tool_runtime_configs_tool_version on tool_runtime_configs(tool_id, tool_version);

create table if not exists user_tool_credentials (
    id varchar(36) primary key,
    user_tool_id varchar(36) not null,
    user_id varchar(36) not null,
    tool_id varchar(36) not null,
    auth_values jsonb,
    created_at timestamp without time zone not null default current_timestamp,
    updated_at timestamp without time zone not null default current_timestamp,
    deleted_at timestamp without time zone
);

create unique index if not exists uk_user_tool_credentials_user_tool
    on user_tool_credentials(user_tool_id)
    where deleted_at is null;

alter table tools add column if not exists runtime_type varchar(50);
alter table tool_versions add column if not exists runtime_type varchar(50);
alter table user_tools add column if not exists runtime_type varchar(50);
alter table user_tools add column if not exists credential_required boolean not null default false;
alter table user_tools add column if not exists credential_configured boolean not null default false;

update tools
set status = 'FAILED',
    reject_reason = '旧 MCP Gateway 工具链路已废弃，请重新提交远程 MCP / HTTP 配置',
    updated_at = now()
where status in ('GITHUB_URL_VALIDATE', 'DEPLOYING', 'FETCHING_TOOLS');

drop table if exists user_containers;
drop table if exists container_templates;
