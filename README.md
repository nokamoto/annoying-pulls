# annoying-pulls

Slack [#annoying-pulls](https://nokamoto.slack.com/messages/annoying-pulls)

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
| exclude-labels | Yes | |

### Slack
| key | mandatory | |
| --- | --- | --- |
| incoming-webhook | Yes | https://api.slack.com/incoming-webhooks |
| channel | No | https://api.slack.com/incoming-webhooks#customizations_for_custom_integrations |
| username | No | https://api.slack.com/incoming-webhooks#customizations_for_custom_integrations |
| icon-emoji | No | https://api.slack.com/incoming-webhooks#customizations_for_custom_integrations |
| warning-after | Yes | |
| danger-after | Yes | |
| attachments-limit | Yes | |
