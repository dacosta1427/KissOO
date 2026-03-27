Process:

**The frontend should support 3 languages: EN, NL and DE**
**There should be hints on the buttons**

The home page should just show the nav bar, logged in status with a green or red ball and the login page as it currently is (bit closer to the navbar though)

Signup:
A new user is also a new Owner (the user is the system-using equivalent of the owner). The two should be connected as in no user *without* an owner, the user should actually be part of the owner.
The signup has to be complemented/completed with an email verification so one should be send contianing a verification link. This is a pretty standard way of working so you can devise the methodology

Login:
## User
On navbar: Profile, Houses, Schedule, etc.?

Sees only his own house(s) and his own bookings per house.
When on a House it has the profile (address and stuff) and view bookings/ Schedules
The (owner)schedule for a House does NOT contain the *cleaners* 
I want card view and table view (toggleble)

## House
Clicking on a house should show its cleaning schedule (only for the house obviously)


## Admin
On navbar: Owners/users, Houses, bookings, cleaners, schedules
Cost profiles: standard and custom (copied of the standard)

Can add a cost profile to every house based on a standard cost profile


## Cleaners
Can see their OWN schedule and they should be able to take pictures/videos of what they see during the cleaning of the house.
Possibly add a note/comment and click the completed button. Als a button for starting the cleaning (config for the admin but should be designed/build)

When i click a + or an existing schedule in the grid i would like the focus to either immediately go to the edit block for the thing just clicked or show a modal. Lets try with the modal first.
