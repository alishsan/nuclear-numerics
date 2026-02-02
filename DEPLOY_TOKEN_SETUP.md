# Setting Up Clojars Deploy Token

## Important: Deploy Token vs Password

Clojars requires a **deploy token**, NOT your account password. The error message "a deploy token is required to deploy" means you need to generate a token.

## Steps to Get Deploy Token

1. **Go to Clojars**: https://clojars.org
2. **Log in** with your account
3. **Click your username** (top right) → **Profile**
4. **Scroll down** to the "Deploy Tokens" section
5. **Click "Generate Token"**
6. **Copy the token immediately** - it starts with `CLOJARS_` and you'll only see it once!

## Using the Token

### Option 1: Enter When Prompted

When you run `lein deploy clojars`:
- **Username**: `alishsan`
- **Password**: Paste your deploy token (the `CLOJARS_...` string)

### Option 2: Save in Credentials File

Update `~/.lein/credentials.clj`:

```clojure
{#"clojars" {:username "alishsan"
             :password "CLOJARS_YOUR_TOKEN_HERE"}}
```

Replace `CLOJARS_YOUR_TOKEN_HERE` with your actual deploy token.

## Verify Token Format

Your deploy token should:
- Start with `CLOJARS_`
- Be a long string of characters
- Be different from your account password

## Troubleshooting

**"a deploy token is required to deploy"**
- You're using your password instead of a token
- Generate a deploy token from your Clojars profile
- Use the token as the password

**"401 Unauthorized"**
- Token might be expired or invalid
- Generate a new token
- Make sure you copied the entire token (no spaces)

## Current Status

✅ Group name is correct: `alishsan/nuclear-numerics`
❌ Need valid deploy token for authentication

Once you have a valid deploy token and use it as the password, the deployment should succeed!
