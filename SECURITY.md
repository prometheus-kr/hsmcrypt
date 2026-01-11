# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in HsmCrypt, please follow these steps:

### DO NOT
- Do not open a public GitHub issue
- Do not disclose the vulnerability publicly until it has been addressed

### DO
1. Email security details to: prometheus@kakao.com
2. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)
3. Allow reasonable time for a response (typically 48 hours)

## Security Considerations

### HSM Configuration
- **NEVER** commit HSM credentials (PIN, passwords) to version control
- Use environment variables or secure vaults for sensitive data
- Regularly rotate HSM PINs

### Encrypted Properties
- HCENC() values are safe to commit (encrypted)
- Ensure your HSM keys are properly secured
- Use appropriate key management practices

### Dependencies
- HsmCrypt uses SIPWON library for HSM operations
- Keep dependencies up to date
- Monitor for security advisories

### Production Usage
- Use proper HSM hardware in production
- Implement proper access controls
- Regular security audits recommended
- Follow your organization's security policies

## Response Timeline
- Initial response: Within 48 hours
- Security patch: Depends on severity
  - Critical: Within 7 days
  - High: Within 14 days
  - Medium: Within 30 days
  - Low: Next regular release

## Disclosure Policy
- Coordinated disclosure preferred
- Public disclosure after patch is available
- Credit given to reporters (unless anonymous)

Thank you for helping keep HsmCrypt secure!
