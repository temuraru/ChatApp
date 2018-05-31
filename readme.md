Mirc-style multi user chat server-client system:

There are some classes :
- GroupHandler - handles group communication
    - each group has an owner - a ClientHandler instance
    the mainHall group has the serverBot special ClientHandler instance
- ClientHandler - handles client

### Commands:
## Guest commands:
- /login username
- /help
- /quit
## Normal user commands:
- /logoff
- /user new_username
- /list - list all available chat groups
- /create <group> <type> # where type can be [public]|private|closed
- /select <group> # set another group as the current group
- /request <group> # request to be added to the group
- /join <group>
- /leave <group>
## Admin commands:
- /groupname <new_name> # change the name of the current group
- /grouptype <new_type> # where new_type can be [public]|private|closed
- /add <group> <user> - add an user to a group
- /invite <group> <user> - invite an user to a group
- /kick <group> <user> [<reason>] - remove an user from a group
- /promote <group> <user> - make an user admin on that group
- /demote <group> <user> - transforms the role of an admin to normal user on that group
## SuperAdmin commands:
- /delete <group>

Server bot jobs:
- When a user logs in, broadcast a message to all users [not affiliated to a group]
- When a user logs off, broadcast a message to all users [not affiliated to a group]
- handle main-hall group
- handle groups' bots
- list all groups

Group bot jobs:
- When a user joins a room, broadcast a message to all users in that group
- When a user leaves a room, broadcast a message to all users in that group
- list all users from the group (ordered by role importance, then alphabetically)

Groups can be:
- public (anyone can join)
- private (one can send a request, but it has to be invited by an admin)
- closed (one can join only after being invited by an admin)

A normal user can:
- create a group - and then it receives super-admin role in that group
- join a group (after an invite message from an admin or simply by using join after list groups command)
- leave a group (by his own or by being kicked)
- send invite requests to the server for private groups (to forward the request to groups' admins)

In a group, a super-admin can:
- not be kicked (being super-admin!)
- invite users to the group (specific message for join group)
- kick users from the group (reason for kicking [because!])
- promote/demote users to/from admin role

In a group, an admin can:
- invite users to the group (specific message for join group)
- kick users from the group (reason for kicking [because!])
- promote/demote users to/from admin role

Client interface:
- if not logged-in, login button with random username prepared
- main group tab active, even for not logged-in clients
- sidebar with available groups and "create new group" section with input field & button



Each tab has a conversation field and am input field + Send button at the bottom
The input field can send messages to the group/another client or commands (statements starting with / or \) to the server
Tab name = group name or username


