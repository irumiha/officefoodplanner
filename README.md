# Office Food Planner 

A application that connects office cooks with their consumers. Design and organise meals, set up seating, track attendance, 
gather feedback.

## For users

You will log in using your company's Oauth2 provider (everyone is using one these days, be it Microsoft Office or Google Apps, etc.)

You will see a list of meals where you can select your preference. There is a time limit for selecting a preference (for 
example, one day before the meal) after which that meal is "locked" and is read-only.

**Very important** Take note of the shift you are in! 

## For cooks

You will log in using your company's Oauth2 provider (everyone is using one these days, be it Microsoft Office or Google Apps, etc.)
Your friendly app admin will make you a cook in the eyes of the application.

You will get the same view as ordinary users but with an option to create new meals and the ability to modify attendance list
after the time limit. There is also a checkmark next to each name that you can use to cross-off people that came to lunch.

Shift editor allows you to add or remove shifts and manually or randomly assign people into shifts. 

## Technical

This application also an experiment that should serve as a blueprint for building new applications. To that end we have defined:

- Programming language: Scala.
- Backend framework: Http4s with Doobie/Postgres for data persistence.
- Frontend: SPA built with Vue.js.
- Migrations: we use FlywayDB.
- Sessions are cookie-based, persisted in the database. 
- Password based auth (not used in the Office Food Planner app)
- Oauth2 for easy integration with other identity providers

When making a new app just clone this one and delete all entities and functionality not related to users and authentication.
But keep the code around as it will provide examples on how to do stuff.

