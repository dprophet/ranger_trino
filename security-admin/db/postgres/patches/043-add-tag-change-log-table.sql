-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

DROP TABLE IF EXISTS x_tag_change_log;
DROP SEQUENCE IF EXISTS x_tag_change_log_seq;

CREATE SEQUENCE x_tag_change_log_seq;

CREATE TABLE x_tag_change_log (
id BIGINT DEFAULT nextval('x_tag_change_log_seq'::regclass),
create_time TIMESTAMP DEFAULT NULL NULL,
service_id bigint NOT NULL,
change_type int NOT NULL,
service_tags_version bigint DEFAULT '0' NOT NULL,
service_resource_id bigint DEFAULT NULL NULL,
tag_id bigint DEFAULT NULL NULL,
primary key (id)
);
commit;
CREATE INDEX x_tag_change_log_IDX_service_id ON x_tag_change_log(service_id);
CREATE INDEX x_tag_change_log_IDX_tag_version ON x_tag_change_log(service_tags_version);
commit;
