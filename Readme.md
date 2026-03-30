# O360 SAST - Android Studio & IntelliJ Plugin

Enterprise-grade Static Application Security Testing (SAST) directly in your IDE.

## Features

- **Project Scanning** — Scan entire projects for security vulnerabilities
- **Git Repository Scanning** — Scan remote Git repositories by URL
- **Severity-Coded Results** — View findings organized by Critical, High, Medium, Low, and Info severity
- **Code Navigation** — Double-click any finding to jump to the vulnerable line
- **Built-in Fix Guidance** — Vulnerability knowledge base with remediation recommendations
- **20+ Languages Supported** — Java, Kotlin, Python, JavaScript, TypeScript, C#, Go, Ruby, PHP, Swift, and more
- **Dependency Scanning** — Detect vulnerable third-party libraries
- **Malware Detection** — Identify malicious code patterns
- **License Compliance** — Check for license violations

## Installation

### From JetBrains Marketplace
1. Open **Settings → Plugins → Marketplace**
2. Search for **"O360 SAST"**
3. Click **Install**, then restart your IDE

### From Disk
1. Download the latest `.zip` from [Releases](https://github.com/offensive360/intellij/releases)
2. Go to **Settings → Plugins → ⚙️ → Install Plugin from Disk**
3. Select the downloaded `.zip` file and restart

## Configuration

1. Go to **Settings → Tools → Offensive360 SAST for Android Studio**
2. Enter your **Server Endpoint** (e.g., `https://sast.offensive360.com`)
3. Paste your **API Access Token**
4. Click **OK**

## Usage

- **Tools → Scan with Offensive 360** to scan the current project
- **Tools → Scan Git Repository** to scan a remote repo
- Use **Ctrl+Shift+A** and search "Scan with Offensive" for quick access
- Results appear in the **Offensive360 Results** tool window at the bottom

## Supported IDEs

- Android Studio (2022.1+)
- IntelliJ IDEA Community & Ultimate (2022.1+)
- All JetBrains IDEs based on IntelliJ Platform

## Requirements

- JetBrains IDE build 221.0 or newer
- Access to an Offensive360 SAST server
- Valid API access token

## Links

- [Offensive360 Website](https://offensive360.com)
- [Knowledge Base](https://knowledge-base.offensive360.com)
- [Support](mailto:support@offensive360.com)
