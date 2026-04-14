# Offensive 360 SAST — IntelliJ / Android Studio Plugin

Deep source code analysis for IntelliJ IDEA and Android Studio. Scan your projects for security vulnerabilities with a single click.

## Requirements

- IntelliJ IDEA 2021.1+ or Android Studio 2021.1+
- An Offensive 360 server instance and valid access token

## Installation

### From Plugin ZIP

1. Download the latest `.zip` from [Releases](https://github.com/offensive360/intellij/releases)
2. In your IDE: **Settings → Plugins → ⚙️ → Install Plugin from Disk**
3. Select the downloaded ZIP and restart the IDE

## Configuration

Go to **Settings → Tools → O360 SAST**:

- **Endpoint**: Your Offensive 360 server URL (e.g. `https://your-server.com`)
- **Access Token**: Generated from the O360 dashboard under Settings → Tokens
- **Allow self-signed SSL certificates**: Enable for on-premise instances
- **Test Connection**: Verify the server is reachable

## How to Use

### Scanning

- **Tools → O360 SAST → Scan Project** to scan the entire project
- **Tools → O360 SAST → Scan Module** to scan a specific module
- **Right-click** a file in the editor → **O360 SAST: Scan File**

### Viewing Results

Results appear in the **O360 Security Findings** panel at the bottom:

- Findings table with Severity, Title, File, Line columns
- **Details tab**: description, impact, affected code snippet
- **How to Fix tab**: remediation steps with CWE references
- **References tab**: OWASP and CWE links
- **Double-click** to navigate to the vulnerable line

### Context Menu

- **Tools → O360 SAST → Check for Updates** — manual update check
- **Tools → O360 SAST → Clear Findings** — remove all results

## Features

- Smart caching: zero server requests when no files changed
- 3 retries with backoff for server errors
- 4-hour timeout for large projects
- Base64 code snippet decoding
- Human-readable vulnerability titles
- Identical file exclusion rules as VS/Eclipse/VSCode plugins
- Auto-update notification (checks GitHub Releases)
- KeepInvisibleAndDeletePostScan — no server traces

## Support

For issues and feature requests, open an issue on this repository.
