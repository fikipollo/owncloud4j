## Python client library for Java

This Java library makes it possible to connect to an ownCloud instance and perform file, share and attribute operations from your Java application.
This library just provides functions to easily send HTTP calls for various ownCloud APIs. This is not a **sync** client implementation.

See the [ownCloud homepage](http://owncloud.org) for more information about ownCloud.

Please note that this library is still on development and that it has been inspired in the [Python client library for ownCloud](https://github.com/owncloud/pyocclient) developed by Vincent Petry and others.

## Features 

### Sessions
- [ ] login
- [ ] logout

### Shared
- [ ] list_open_remote_share
- [ ] accept_remote_share
- [ ] decline_remote_share
- [ ] delete_share
- [ ] update_share
- [ ] accept_remote_share
- [ ] share_file_with_link
- [ ] is_shared
- [ ] get_share
- [ ] get_shares

### Files
- [x] list
- [x] file_info
- [x] get_file_contents
- [x] get_file
- [ ] get_directory_as_zip
- [ ] put_file_contents
- [x] put_file
- [ ] put_directory
- [ ] mkdir
- [ ] delete
- [ ] move
- [ ] copy
- [ ] move

### Users
- [ ] create_user
- [ ] delete_user
- [ ] user_exists
- [ ] search_users
- [ ] get_users
- [ ] set_user_attribute
- [ ] add_user_to_group
- [ ] get_user_groups
- [ ] user_is_in_group
- [ ] get_user
- [ ] remove_user_from_group
- [ ] add_user_to_subadmin_group
- [ ] get_user_subadmin_groups
- [ ] user_is_in_subadmin_group
- [ ] share_file_with_user
- [ ] create_group
- [ ] delete_group
- [ ] get_groups
- [ ] get_group_members
- [ ] group_exists
- [ ] share_file_with_group

### Admin
- [ ] get_config
- [ ] get_attribute
- [ ] set_attribute
- [ ] delete_attribute
- [ ] get_apps
- [ ] get_version
- [ ] get_capabilities
- [ ] enable_app
- [ ] disable_app


