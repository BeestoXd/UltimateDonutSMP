# Contributing to UltimateDonutSMP

Thank you for your interest in contributing to **UltimateDonutSMP**! We welcome bug reports, feature requests, code improvements, and pull requests from the community.

---

## Code of Conduct & Rules

- **Respect Licensing**: UltimateDonutSMP is released under a proprietary license. Contributions submitted to this repository become part of the project under its existing licensing terms.
- **Maintain Clean Code**: Follow existing Java and Maven project structures. Write readable, well-structured, and documented code.
- **Test Before Submitting**: Ensure your code builds cleanly (`mvn clean package`) and does not introduce regression errors.

---

## How to Contribute

### 1. Reporting Issues & Suggestions
- Search existing [GitHub Issues](https://github.com/BeestoXd/UltimateDonutSMP/issues) before opening a new one to avoid duplicates.
- Provide detailed information including server software, plugin version, stack traces (if applicable), and steps to reproduce.

### 2. Submitting Pull Requests (PRs)
1. **Fork** the repository and create a new branch for your feature or bug fix:
   ```bash
   git checkout -b feature/my-new-feature
   ```
2. **Make your changes** adhering to the project's code style and conventions.
3. **Build and Test**:
   ```bash
   mvn clean package
   ```
4. **Commit your changes** with clear, descriptive commit messages.
5. **Push to your fork** and submit a **Pull Request** to the `main` branch.

---

## Development Workflow

- **Java Version**: Targets Java 21+ (Paper/Spigot 1.21.10 - 26.2, Folia 1.21.11 - 26.2).
- **Build System**: Apache Maven (`mvn`).
- **Dependencies**: Soft dependencies (e.g. PlaceholderAPI, Vault, LuckPerms, ProtocolLib) should be handled safely using optional integration checks.

---

Thank you for helping make UltimateDonutSMP better!
