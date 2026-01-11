# Contributing to HsmCrypt

Thank you for your interest in contributing to HsmCrypt!

## How to Contribute

### Reporting Issues
- Check if the issue already exists
- Provide detailed information about the problem
- Include Java version, Spring Boot version, and HSM environment details
- Share relevant logs and error messages

### Submitting Pull Requests
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests: `mvn test`
5. Format code: `.\format.ps1` (Windows) or `mvn spotless:apply`
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

### Code Style
- Follow the existing code style
- Use Eclipse formatter configuration (`eclipse-formatter.xml`)
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed

### Development Setup
1. Java 21 required
2. Maven 3.6+
3. Build: `.\build.ps1` or `mvn clean install`
4. Format: `.\format.ps1` or `mvn spotless:apply`

### Testing
- Write unit tests for new features
- Ensure all tests pass before submitting PR
- Run: `mvn test`

## Code of Conduct
- Be respectful and constructive
- Focus on the issue, not the person
- Accept constructive criticism

## Questions?
- Open an issue for discussion
- Contact: prometheus@kakao.com

Thank you for contributing! 🎉
