# Toggl Redmine integration

Integration between Redmine and Toggl written in Kotlin. Just prefix name of the toggle time entry with 
redmine issue number and let the app track your spent time for you!

## Config

You need to setup [`config.json`](./config.json) for access to Redmine api and Toggl api. Json looks like
```json
{
  "toggl_apikey": "xxx", // api key, obtained in Profile settings / Api Token  
  "workspace_id": "xxx", // id of your workspace in toggl. Can be found in url in Reports - https://toggl.com/app/reports/summary/$workspace_id.
  "email": "xx@yy.com", // email used in toggl user_agent. In theory it can be whatever string you want
  "redmine_api_key": "xxx", // redmine api key. Can be found in https://redmine_url/my/account as API access key
  "redmine_base_url": "https://redmine_url" // base url to redmine 
}
```


## Usage

Prefix time entries in toggl with redmine issue number and the app will read your time entries, parse the issue number from the
time entry and track it as a spent time to redmine.

The app will read all time entries that does not have a tag `billed`. Once the time entry is tracked to redmine it's tagged
with this `billed` tag.