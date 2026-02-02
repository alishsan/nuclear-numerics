# Troubleshooting Clojars Deployment

## Current Error
```
403 Forbidden - Group 'alishsan' doesn't exist
```

## Step-by-Step Troubleshooting

### Step 1: Verify You Have a Deploy Token

1. Go to https://clojars.org
2. Log in with your account
3. Click on your profile/username
4. Go to "Deploy Tokens" or visit https://clojars.org/tokens
5. If you don't have one, create a new token
6. **Copy the entire token** (it's long, starts with `CLOJARS_`)

### Step 2: Try Environment Variables (Bypass Credentials File)

Instead of entering credentials interactively, use environment variables:

```bash
export CLOJARS_USERNAME=alishsan
export CLOJARS_PASSWORD=CLOJARS_YOUR_FULL_TOKEN_HERE
cd /Users/a.sanetullaev/Development/clojure/nuclear-numerics
lein deploy clojars
```

**Important:** 
- Replace `CLOJARS_YOUR_FULL_TOKEN_HERE` with your actual token
- No quotes needed around the token
- Make sure there are no extra spaces

### Step 3: Verify Token Format

Your deploy token should:
- Start with `CLOJARS_`
- Be very long (usually 40+ characters)
- Not contain spaces
- Be copied exactly as shown (no line breaks)

### Step 4: Check Token Permissions

Make sure your token has:
- Deploy permissions (should be default)
- Not expired
- Not been revoked

### Step 5: Try a Fresh Token

If the token doesn't work:
1. Revoke the old token
2. Generate a completely new one
3. Copy it immediately
4. Try deploying again

### Step 6: Verify Project Configuration

The `project.clj` should have:
```clojure
(defproject alishsan/nuclear-numerics "0.1.0-SNAPSHOT"
  ...
  :deploy-repositories [["clojars" {:url "https://repo.clojars.org"
                                     :sign-releases false}]]
```

### Step 7: Check Clojars Status

Sometimes Clojars has temporary issues. Check if there are any service disruptions.

## Alternative: Use Credentials File

If environment variables don't work, try setting up the credentials file:

1. Create/edit `~/.lein/credentials.clj.gpg` (encrypted) or `~/.lein/credentials.clj` (plain text - less secure)

2. For plain text (not recommended for production):
```clojure
{#"clojars" {:username "alishsan"
             :password "CLOJARS_YOUR_TOKEN_HERE"}}
```

3. For encrypted (recommended):
```bash
# Install gpg if needed
# Then create encrypted file
gpg --encrypt --recipient your-email@example.com ~/.lein/credentials.clj
```

## Still Not Working?

If none of the above works:

1. **Double-check the token:** Generate a brand new one and try again
2. **Check your Clojars account:** Make sure your account is active
3. **Try a different group format:** Some users report success with reverse-domain notation like `com.github.alishsan/nuclear-numerics`
4. **Contact Clojars support:** If authentication is definitely correct but still failing

## Expected Success Output

When deployment succeeds, you should see:
```
Sending alishsan/nuclear-numerics/0.1.0-SNAPSHOT/nuclear-numerics-0.1.0-XXX.jar
    to https://repo.clojars.org/
Sending alishsan/nuclear-numerics/0.1.0-SNAPSHOT/nuclear-numerics-0.1.0-XXX.pom
    to https://repo.clojars.org/
```

No errors about groups or authentication.
