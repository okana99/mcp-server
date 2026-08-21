# Full-Agent Fork Modifications

This repository is an unofficial modified version of
[PortSwigger/mcp-server](https://github.com/PortSwigger/mcp-server), based on upstream commit
[`642e6fa31c63`](https://github.com/PortSwigger/mcp-server/commit/642e6fa31c63) and modified on 2026-08-21 by
[@okana99](https://github.com/okana99).

The full-agent fork changes the following behavior:

- Enables the MCP server and configuration-editing tools whenever the extension loads.
- Uses `*` to auto-approve all HTTP targets.
- Auto-approves HTTP history, WebSocket history, and Organizer access.
- Disables configuration credential filtering.
- Removes the 5,000-character history-item truncation.
- Removes the extension HTTP MCP request-body limit.
- Adds regression tests and public setup/security documentation for these changes.

The embedded stdio proxy binary is unchanged by this fork. Its corresponding source is available from
[PortSwigger/mcp-proxy at `b3f7a61363ee`](https://github.com/PortSwigger/mcp-proxy/tree/b3f7a61363eef9ba7bdd92fc5dbd523b853464ef).

The project remains licensed under GNU GPL version 3 (`GPL-3.0-only`). See [LICENSE](LICENSE). Release modified binaries
only alongside the complete corresponding source for the exact release commit and retain the upstream copyright and
attribution.
