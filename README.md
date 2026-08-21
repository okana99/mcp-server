# Burp Suite MCP Server Extension — Full-Agent Fork

> [!WARNING]
> This is an unofficial, intentionally permissive fork of
> [PortSwigger/mcp-server](https://github.com/PortSwigger/mcp-server). It is intended for isolated security-testing
> environments and targets you are authorized to test. Do not expose the MCP listener beyond loopback, and do not use
> the unrestricted agent commands below on an untrusted workstation or repository.

Modified on 2026-08-21 by [@okana99](https://github.com/okana99), based on upstream commit
[`642e6fa`](https://github.com/PortSwigger/mcp-server/commit/642e6fa31c63). See [MODIFICATIONS.md](MODIFICATIONS.md)
for the concrete changes and corresponding-source references.

## Overview

Integrate Burp Suite with AI Clients using the Model Context Protocol (MCP).

For more information about the protocol visit: [modelcontextprotocol.io](https://modelcontextprotocol.io/)

This fork applies a full-agent profile whenever the extension loads:

- Enables the MCP server and configuration-editing tools.
- Auto-approves every HTTP target using `*`.
- Allows HTTP history, WebSocket history, and Organizer access without approval dialogs.
- Disables configuration credential filtering.
- Removes the extension's 5,000-character history-item truncation and HTTP MCP request-body limit.

The controls can still be changed during a Burp session, but the full-agent profile is restored the next time the
extension loads. Client, transport, JVM memory, and stdio proxy limits can still apply.

## Features

- Connect Burp Suite to AI clients through MCP
- Automatic installation for Claude Desktop
- Comes with packaged Stdio MCP proxy server

## Usage

- Install the extension in Burp Suite
- Configure your Burp MCP server in the extension settings
- Configure your MCP client to use the Burp SSE MCP server or stdio proxy
- Interact with Burp through your client!

## Installation

### Prerequisites

Ensure that the following prerequisites are met before building and installing the extension:

1. **Java**: Java must be installed and available in your system's PATH. You can verify this by running `java --version` in your terminal.
2. **jar Command**: The `jar` command must be executable and available in your system's PATH. You can verify this by running `jar --version` in your terminal. This is required for building and installing the extension.

### Building the Extension

1. **Clone your fork** into the path used by the example MCP configuration:
   ```
   git clone https://github.com/okana99/mcp-server.git /home/user/mcp/mcp-server
   ```

2. **Navigate to the Project Directory**: Move into the project's root directory.
   ```
   cd /home/user/mcp/mcp-server
   ```

3. **Build the JAR File**: Use Gradle to build the extension.
   ```
   ./gradlew embedProxyJar
   ```

   This command compiles the source code and packages it into a JAR file located in `build/libs/burp-mcp-all.jar`.

### Loading the Extension into Burp Suite

1. **Open Burp Suite**: Launch your Burp Suite application.
2. **Access the Extensions Tab**: Navigate to the `Extensions` tab.
3. **Add the Extension**:
    - Click on `Add`.
    - Set `Extension Type` to `Java`.
    - Click `Select file ...` and choose `/home/user/mcp/mcp-server/build/libs/burp-mcp-all.jar`.
    - Click `Next` to load the extension.

> [!IMPORTANT]
> Load `build/libs/burp-mcp-all.jar` into Burp. Do not load `libs/mcp-proxy-all.jar`; that file is only the stdio proxy
> launched by an MCP client.

Upon successful loading, the MCP Server Extension will be active within Burp Suite.

## Configuration

### Configuring the Extension
Configuration for the extension is done through the Burp Suite UI in the `MCP` tab.
- **Toggle the MCP Server**: The `Enabled` checkbox controls whether the MCP server is active.
- **Enable config editing**: The `Enable tools that can edit your config` checkbox allows the MCP server to expose tools which can edit Burp configuration files.
- **Advanced options**: You can configure the port and host for the MCP server. By default, it listens on `http://127.0.0.1:9876`.

### Claude Desktop Client

To fully utilize the MCP Server Extension with Claude, you need to configure your Claude client settings appropriately.
The extension has an installer which will automatically configure the client settings for you.

1. This setup connects Claude Desktop through a stdio proxy. Claude starts the proxy process, which forwards MCP
   messages to the Burp instance listening at a known local address (`127.0.0.1:9876`).

2. **Configure Claude to use the Burp MCP server**  
   You can do this in one of two ways:

    - **Option 1: Run the installer from the extension**
      This will add the Burp MCP server to the Claude Desktop config.

    - **Option 2: Manually edit the config file**  
      Open `~/.config/Claude/claude_desktop_config.json` on Linux or
      `~/Library/Application Support/Claude/claude_desktop_config.json` on macOS, then replace or update it with the
      following:
      ```json
      {
        "mcpServers": {
          "burp": {
            "command": "/home/user/BurpSuite/jre/bin/java",
            "args": [
                "-jar",
                "/home/user/mcp/mcp-server/libs/mcp-proxy-all.jar",
                "--sse-url",
                "http://127.0.0.1:9876"
            ]
          }
        }
      }
      ```

3. **Restart Claude Desktop** - assuming Burp is running with the extension loaded.

### Project `.mcp.json`

The repository includes a ready-to-copy [`.mcp.json`](.mcp.json) for Claude Code and other clients that support the
`mcpServers` JSON format. It is immediately usable when Burp and this repository are installed at the `/home/user`
paths shown below; otherwise, change both absolute paths first.

```json
{
  "mcpServers": {
    "burp": {
      "command": "/home/user/BurpSuite/jre/bin/java",
      "args": [
        "-jar",
        "/home/user/mcp/mcp-server/libs/mcp-proxy-all.jar",
        "--sse-url",
        "http://127.0.0.1:9876"
      ]
    }
  }
}
```

Keep Burp running with the extension enabled before starting the MCP client.

### Codex CLI

Codex supports MCP but uses `~/.codex/config.toml` or a trusted project's `.codex/config.toml` rather than
`.mcp.json`. Register this stdio proxy once:

```bash
codex mcp add burp -- \
  /home/user/BurpSuite/jre/bin/java \
  -jar /home/user/mcp/mcp-server/libs/mcp-proxy-all.jar \
  --sse-url http://127.0.0.1:9876
```

Verify it with `codex mcp list` or `/mcp` inside Codex. Official configuration documentation is available in the
[Codex MCP guide](https://developers.openai.com/codex/mcp/).

### Unrestricted agent mode

Only inside a dedicated disposable VM or similarly isolated environment, the clients can be started without their
normal approval protections:

```bash
codex --dangerously-bypass-approvals-and-sandbox
claude --dangerously-skip-permissions
```

These modes bypass the client's protection layer, while this fork also bypasses Burp MCP approval dialogs. Together,
they can execute commands as the current OS user, send requests to any HTTP target, and expose unfiltered Burp history
or configuration credentials. Treat every prompt, repository file, HTTP response, and MCP output as potentially
hostile. These flags launch the clients; they do not belong in `.mcp.json` or the MCP server command.

## Manual installations
If you want to install the MCP server manually you can either use the extension's SSE server directly or the packaged
Stdio proxy server.

### SSE MCP Server
To use the SSE server directly, provide the configured server URL to your MCP client:
```
http://127.0.0.1:9876
```

### Stdio MCP Proxy Server
The source code for the proxy server can be found here: [MCP Proxy Server](https://github.com/PortSwigger/mcp-proxy)

In order to support MCP Clients which only support Stdio MCP Servers, the extension comes packaged with a proxy server for
passing requests to the SSE MCP server extension.

If you want to use the Stdio proxy server you can use the extension's installer option to extract the proxy server jar.
Once you have the jar you can add the following command and args to your client configuration:
```
/home/user/BurpSuite/jre/bin/java -jar /home/user/mcp/mcp-server/libs/mcp-proxy-all.jar --sse-url http://127.0.0.1:9876
```

If you modify the proxy source, rebuild and copy it into this project before packaging the extension:
```bash
# From mcp-proxy
./gradlew shadowJar
cp build/libs/mcp-proxy-all.jar /home/user/mcp/mcp-server/libs/mcp-proxy-all.jar

# From mcp-server
./gradlew embedProxyJar
```

### Creating / modifying tools

Tools are defined in `src/main/kotlin/net/portswigger/mcp/tools/Tools.kt`. To define new tools, create a new serializable
data class with the required parameters which will come from the LLM.

The tool name is auto-derived from its parameters data class. A description is also needed for the LLM. You can return
a string or a `List<ContentBlock>` to provide data back to the LLM.

Extend the Paginated interface to add auto-pagination support.

## License and attribution

This project is derived from [PortSwigger/mcp-server](https://github.com/PortSwigger/mcp-server) and remains licensed
under GNU GPL version 3 (`GPL-3.0-only`). Keep the [LICENSE](LICENSE), upstream attribution, and corresponding source
available when redistributing modified builds.
