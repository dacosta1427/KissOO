[![](Kiss-logo.svg)](https://kissweb.org)

# KISS Web Application Full Stack Framework

**This is a fork of KISS with Perst OODB integration.**

The KISS Framework is a Java-based, full-stack application development framework for
developing web-based business applications. KISS can also be used to
build command-line utilities, and, in conjunction with
[Electron](https://electronjs.org), desktop applications that are
portable to Windows, macOS, and Linux.

This fork adds [Perst](http://www.garret.ru/perst.html) OODB (Object-Oriented Database)
integration for fast, embedded persistence.

## Quick Start

### Linux, macOS, BSD, etc.

    git clone https://github.com/dacosta1427/KissOO.git
    cd KissOO
    ./bld develop

### Windows

    git clone https://github.com/dacosta1427/KissOO.git
    cd KissOO
    bld develop

Then open `http://localhost:8000` in your browser.

### Enable Perst (Optional)

Perst is disabled by default. To enable:

1. Edit `backend/application.ini` and add:
   ```
   perst.enabled=true
   ```
2. Restart the application

### Update Perst

To download the latest Perst RELEASE version from the Maven repository:

```bash
./bld perst-update
```

Or using Maven directly:

```bash
mvn antrun:run@perst-update
```

Both commands download the latest `perst-dcg.jar` to the `libs/` directory.

## Project Structure

```
src/main/
├── precompiled/          # Code that doesn't change often
│   ├── mycompany/domain/     # Domain entities (Actor, Agreement, Group, PerstUser)
│   ├── mycompany/database/   # Manager classes (ActorManager, PerstHelper)
│   └── oodb/                 # Perst configuration (PerstConfig, PerstContext)
└── backend/
    └── services/            # REST services (frequently changing)
```

## Testing

Run all tests with:

    java -jar work/KissUnitTest.jar --select-package=oodb --select-package=org.kissweb

See [docs/TestingGuide.md](docs/TestingGuide.md) for detailed testing instructions.

## Book

For a comprehensive, tutorial-style guide, see
[Kiss: A Complete Guide to the Web Application Framework](https://a.co/d/035V1VEl)
by Blake McBride, available on Amazon.

## Documentation

- **Testing Guide**: [docs/TestingGuide.md](docs/TestingGuide.md)
- **Perst Integration**: [docs/PerstIntegration.md](docs/PerstIntegration.md)
- **Manager at the Gate**: [MANAGER_AT_THE_GATE.md](MANAGER_AT_THE_GATE.md)
- **Perst Usage**: [PERST_USAGE.md](PERST_USAGE.md)

## Main Project

For the original KISS Framework, visit:
- Website: [kissweb.org](https://kissweb.org)
- Upstream repo: [github.com/blakemcbride/Kiss](https://github.com/blakemcbride/Kiss)
- Support: [Kiss Support](https://github.com/blakemcbride/Kiss/discussions)

## Support

For issues with this fork, please open an issue on GitHub.
