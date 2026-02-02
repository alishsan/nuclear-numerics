# Authentication Issue: 401 Unauthorized

## Problem

Even when entering credentials manually, you're getting:
```
authentication failed ... status: 401 Unauthorized
```

This means the credentials themselves are being rejected by Clojars.

## Possible Causes

### 1. Token Expired or Invalid
- Clojars deploy tokens can expire
- The token might have been revoked
- Check your Clojars profile to verify the token is still active

### 2. Wrong Token Type
- Make sure you're using a **Deploy Token**, not an API token
- Deploy tokens are specifically for `lein deploy`

### 3. Token Format Issue
- Make sure there are no extra spaces or newlines
- The token should start with `CLOJARS_` and be one continuous string

### 4. Username Mismatch
- Verify your Clojars username is exactly `alishsan`
- Check at https://clojars.org (your profile page)

## Solution: Generate a New Token

1. Go to https://clojars.org
2. Log in
3. Click your username → Profile
4. Scroll to "Deploy Tokens"
5. **Delete the old token** (if it exists)
6. Click "Generate Token"
7. **Copy the new token immediately** (you'll only see it once!)

## Update Credentials

Once you have a new token, update `~/.lein/credentials.clj`:

```clojure
{#"clojars" {:username "alishsan"
             :password "NEW_TOKEN_HERE"}}
```

Then try again:
```bash
cd nuclear-numerics
lein deploy clojars
```

## Alternative: Use Leiningen's Built-in Prompt

If the file still doesn't work, you can just enter credentials when prompted. The 401 error suggests the token itself needs to be regenerated.
