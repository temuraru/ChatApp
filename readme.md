Mirc-style multi user chat server-client system:

There are some classes :
- GroupHandler - handles group communication
    - each group has an owner - a ClientHandler instance
    the mainHall group has the serverBot special ClientHandler instance
- ClientHandler - handles client

### Commands:
## Normal user commands:
- login username
- logoff
- list - list all available chat groups
- create <group>
- request <group>
- join <group>
- leave <group>
## Admin commands:
- add <group> <user> - add an user to a group
- kick <group> <user> [<reason>] - remove an user from a group
- delete <group>
- promote <group> <user> - make an user admin on that group
- demote <group> <user> - transforms the role of an admin to normal user on that group

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

