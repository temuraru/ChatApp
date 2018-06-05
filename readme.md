Mirc-style multi user chat server-client system:

There are some classes :
- GroupHandler - handles group communication
    - each group has an owner - a ClientHandler instance
    the mainHall group has the serverBot special ClientHandler instance
- ClientHandler - handles client

# Commands:
!Any line that doesn't begin with "/" is considered a text to be broadcast to the current group
## Guest commands:
- /help
- /quit
- /login <user_name>
- /list - list all available chat groups
- /speak <message> - deliver message to the current group
- /info - display user & group info
## Normal user commands:
- /logoff # = /quit
- /user <new_username>
- /create <group> [<type>] # where type can be [public]|private|closed
- /select <group> # set another group as the current group (if already member)
- /request <group> # request to be added to a PRIVATE group
- /join <group> # join a PUBLIC group
- /leave <group>
- /accept <group> - accept an invite sent by an admin from a CLOSED group
- /talk <user_name> <message> - deliver <message> to client <user_name>
- /block <user_name> - block client <user_name>
- /unblock <user_name> - unblock client <user_name>
## Admin commands:
- /groupname <new_name> # change the name of the current group
- /grouptype <new_type> # where new_type can be [public]|private|closed
- /add <user> [<group>] - add an user to a group
- /kick <user> [<group>] [<reason>] - remove an user from a group
- /promote <user> [<group>] - make an user admin on that group
- /demote <user> [<group>] - transforms the role of an admin to normal user on that group
- /invite <user> [<group>] - invite an user to a PRIVATE group
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
- join a public group (or save an interest about private/closed groups)
- leave a group (by his own or by being kicked)
- send invite requests to private groups (to the groups' admins)
- accept invites from admins in closed groups

In a group, an admin can:
- invite users to the group (specific message for join group)
- kick users from the group (reason for kicking [because!])
- promote/demote users to/from admin role

In a group, a super-admin can:
- not be kicked (being super-admin!)
- invite users to the group (specific message for join group)
- kick users from the group (reason for kicking [because!])
- promote/demote users to/from admin role

Client interface:
- if not logged-in, login button with random username prepared
- main group tab active, even for not logged-in clients
- sidebar with available groups and "create new group" section with input field & button


## Run commands examples: 
### server:
REM "C:\Program Files\Java\jdk-9.0.4\bin\java.exe" -javaagent:C:\Users\teodor.muraru\AppData\Local\JetBrains\Toolbox\apps\IDEA-C\ch-0\181.5087.20\lib\idea_rt.jar=52581:C:\Users\teodor.muraru\AppData\Local\JetBrains\Toolbox\apps\IDEA-C\ch-0\181.5087.20\bin -Dfile.encoding=UTF-8 -classpath D:\work\IdeaProjects\retele\proiect\ChatApp\out\production\ServerApp;D:\work\IdeaProjects\retele\proiect\ChatApp\ServerApp\lib\commons-lang3-3.7.jar com.temuraru.ServerMain
java.exe -javaagent:"C:\Users\teodor.muraru\AppData\Local\JetBrains\Toolbox\apps\IDEA-C\ch-0\181.5087.20\lib\idea_rt.jar"=52581:"C:\Users\teodor.muraru\AppData\Local\JetBrains\Toolbox\apps\IDEA-C\ch-0\181.5087.20\bin" -Dfile.encoding=UTF-8 -classpath "D:\work\IdeaProjects\retele\proiect\ChatApp\out\production\ServerApp;D:\work\IdeaProjects\retele\proiect\ChatApp\ServerApp\lib\commons-lang3-3.7.jar" com.temuraru.ServerMain
Server started on port: 8867!
### client/console:
telnet localhost 8867


## TODO:
Each tab has a conversation field and am input field + Send button at the bottom
The input field can send messages to the group/another client or commands (statements starting with / or \) to the server
Tab name = group name or username


