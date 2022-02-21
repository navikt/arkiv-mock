# arkiv-mock
This component acts as a mock for Joark. It is intended to be used for end-to-end and load tests, but not in production.

The components' behaviour can be controlled, so that it can for instance respond with errors a certain amount of times.
When the component receives data, it will broadcast information about the data to Kafka topics, where interested consumers (i.e. the [system-tests](https://github.com/navikt/archiving-infrastructure)) can listen.

For a description of the whole archiving system,
see [the documentation](https://github.com/navikt/archiving-infrastructure/wiki).

## Inquiries
Questions regarding the code or the project can be asked to the team by [raising an issue on the repo](https://github.com/navikt/arkiv-mock/issues).

### For NAV employees
NAV employees can reach the team by Slack in the channel #teamsoknad
