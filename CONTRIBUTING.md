# Contributing to UltimateDonutSmp

Thank you for your interest in contributing to **UltimateDonutSmp**! We welcome bug reports, feature requests, documentation fixes, and pull requests from the community.

---

## Code of Conduct & Rules

- **Respect Licensing**: UltimateDonutSmp is released under a proprietary license. Contributions submitted to this repository become part of the project under its existing licensing terms. See [LICENSE.md](LICENSE.md).
- **Maintain Clean Code**: Follow the existing Java and Maven project structure. Write readable, well-structured code and keep new code consistent with the surrounding style.
- **Test Before Submitting**: Make sure the project builds and the unit tests pass (`mvn clean test`, then `mvn package -DskipTests`) before opening a pull request.
- **Keep Docs In Sync**: If you add or change a command, permission node, or configuration key, update [README.md](README.md) and the matching page under [`docs/wiki/`](docs/wiki) in the same pull request.

---

## Development Environment

| Item | Value |
| --- | --- |
| Language level | Java 21 (`maven.compiler.release`) |
| CI toolchain | JDK 25 (Temurin), as used by `.github/workflows/pr-checks.yml` |
| Build system | Apache Maven (`mvn`) |
| Target platforms | Paper/Spigot `1.21.10` – `26.2`, Folia `1.21.11` – `26.2` |
| Hard dependencies | PlaceholderAPI, ProtocolLib (declared under `depend` in `plugin.yml`) |
| Soft dependencies | LuckPerms, Vault, Apollo, Multiverse-Core, floodgate, SkinsRestorer |
| Unit tests | JUnit 5, under `src/test/java` |

Guard every soft dependency behind a runtime availability check (for example
`Bukkit.getPluginManager().isPluginEnabled("LuckPerms")`) so the plugin still loads when it is absent.
Hard dependencies may be used directly.

Useful commands:

```bash
mvn clean test
```

```bash
mvn package -DskipTests
```

On Windows you can also run `build.bat`, which wraps `mvn clean package`.
The build produces a single shaded jar in `target/` that detects Paper, Spigot, or Folia at runtime.

---

## How to Contribute

### 1. Reporting Issues & Suggestions

- Search the existing [GitHub Issues](https://github.com/BeestoXd/UltimateDonutSMP/issues) before opening a new one to avoid duplicates.
- Use the matching issue form: **Bug Report**, **Feature Request**, or **Documentation**. Blank issues are disabled.
- Include the plugin version, server software and version, Java version, stack traces, and clear reproduction steps.
- For setup questions and general help, use the [Discord community](https://dsc.gg/hellstarr) instead of the issue tracker.
- Never post database credentials, Redis passwords, Discord webhook URLs, or other secrets in an issue.

### 2. Submitting Pull Requests (PRs)

1. **Fork** the repository and create a branch for your change:
   ```bash
   git checkout -b fix/my-bug-fix
   ```
2. **Make your changes**, following the project's existing conventions.
3. **Build and test locally**:
   ```bash
   mvn clean test
   ```
4. **Commit** with a clear, descriptive message.
5. **Push to your fork** and open a **Pull Request** against the `main` branch.
6. Fill in the pull request description. An empty or very short description fails CI.

### 3. Pull Request Requirements

Every PR runs `.github/workflows/pr-checks.yml`, which enforces:

- **Conventional PR title** — `type(scope): summary`, where `type` is one of
  `feat`, `fix`, `chore`, `docs`, `style`, `refactor`, `perf`, `test`, `ci`, `build`.
  A scope is optional but encouraged, e.g. `fix(folia): stop resolving inventory holders off-region`.
- **Non-empty PR description** — at least 10 characters of real explanation.
- **Build & test on JDK 25** — `mvn clean test` followed by `mvn package -DskipTests`.
- **Dependency review scan** on any dependency change.

Templates are available under `.github/PULL_REQUEST_TEMPLATE/` for bug fixes, features, and
maintenance work. Reference the issue your PR closes (for example `Closes #111`).

---

## Documentation Contributions

Long-form documentation lives in [`docs/wiki/`](docs/wiki) and mirrors the published GitHub Wiki:

- `Commands-and-Permissions.md` — command syntax, aliases, and permission nodes
- `Configuration-Reference.md` and the `Config-*.yml.md` pages — per-file configuration keys
- Feature guides such as `Economy-and-Marketplaces.md`, `Duels-and-FFA.md`, and `Staff-and-Security.md`

Keep the command and permission tables in `README.md`, `docs/wiki/Commands-and-Permissions.md`,
and `src/main/resources/plugin.yml` consistent with one another.

---

Thank you for helping make UltimateDonutSmp better!
