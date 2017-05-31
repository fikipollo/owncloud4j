## Python client library for Java

This Java library makes it possible to connect to an ownCloud instance and perform file, share and attribute operations from your Java application.
This library just provides functions to easily send HTTP calls for various ownCloud APIs. This is not a **sync** client implementation.
All WebDAV operations are performed using the [Apache Jackrabbit](http://jackrabbit.apache.org/) library.
Most functions have been developed based on the [Python client library for ownCloud](https://github.com/owncloud/pyocclient) developed by Vincent Petry and others.

Please note that this library is still on development, see the "Features" section below to get a complete list of the features that are currently developed.

See the [ownCloud homepage](http://owncloud.org) for more information about ownCloud.

## Usage
Example for file and directory manipulation:

```java

Owncloud4j client = new Owncloud4j("localhost", 8085, "/remote.php/webdav");

client.login("rafa", "supersecret");

//Create a new dirs
boolean success = client.mkdir("/testdir/");
success = client.mkdir("/testdir/child_1");

//Upload local file 
success = client.putFile("/testdir/", "/data/test/test.txt");

//List directories with max depth 3 child 
WebDAVResponse response = client.list("/testdir/", 3);

//Get details for an specific file
response = client.fileInfo("/testdir/test.txt");

//Get content of a file as string
String content = client.getFileContents("/testdir/test.txt");

//Download a file to local directory 
success = client.getFile("/testdir/test.txt", "/tmp/");

//Delete remote files or dir
success = client.delete("/testdir/");

```

## Features 

### Sessions
- [x] login
- [x] logout

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
- [x] mkdir
- [x] delete
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


