# annoying-pulls

![slack thumbnail v0.2](https://cloud.githubusercontent.com/assets/4374383/23327713/53d602d2-fb54-11e6-9e5e-8ef4cce63576.png)

[![Build Status](https://travis-ci.org/nokamoto/annoying-pulls.svg?branch=master)](https://travis-ci.org/nokamoto/annoying-pulls)

Slack [#annoying-pulls](https://nokamoto.slack.com/messages/annoying-pulls)

```
sbt assembly
java -Dconfig.file=application.conf -jar target/annoying-pulls-0.2.2-SNAPSHOT.jar
```

## Settings
```
github {
  api = "https://api.github.com"

  org = null

  username = "nokamoto"

  excluded-labels = ["duplicate", "invalid", "wontfix"]
}

slack {
  incoming-webhook = "https://hooks.slack.com/services/..."

  channel = "annoying-pulls"

  username = "annoying-pulls-webhook"

  icon-emoji = ":anger:"

  comment-icon-emoji = ":speech_balloon:"

  warning-after = 7 days

  danger-after = 14 days

  attachments-limit = 20
}
```


### Github
| key | mandatory | |
| --- | --- | --- |
| api | Yes | https://developer.github.com/v3/#schema |
| org | No | https://developer.github.com/v3/repos/#list-organization-repositories |
| username | No | https://developer.github.com/v3/repos/#list-user-repositories |
| exclude-labels | Yes | filter pull requests by label |

### Slack
| key | mandatory | |
| --- | --- | --- |
| incoming-webhook | Yes | https://api.slack.com/incoming-webhooks |
| channel | No | https://api.slack.com/incoming-webhooks#customizations_for_custom_integrations |
| username | No | https://api.slack.com/incoming-webhooks#customizations_for_custom_integrations |
| icon-emoji | No | https://api.slack.com/incoming-webhooks#customizations_for_custom_integrations |
| comment-icon-emoji | Yes | customize a comment icon in footer |
| warning-after | Yes | attach n days old pull requests with `warning` color |
| danger-after | Yes | attach n days old pull requests with `danger` color |
| attachments-limit | Yes | never send more than [20](https://api.slack.com/docs/message-guidelines) attachments |
