# Troubleshooting Clojars Credentials

## Issue: Leiningen Not Finding Credentials

If `lein deploy clojars` prompts for username/password even though you have `~/.lein/credentials.clj`, try these solutions:

### Solution 1: Verify File Format

The credentials file should be exactly:

```clojure
{#"clojars" {:username "YOUR_USERNAME"
             :password "YOUR_TOKEN"}}
```

**Important:**
- Use a regex pattern: `#"clojars"` (with #"")
- Use a map: `{:username "..." :password "..."}`
- File must be valid Clojure code

### Solution 2: Check File Permissions

```bash
chmod 600 ~/.lein/credentials.clj
ls -la ~/.lein/credentials.clj
# Should show: -rw------- (read/write for owner only)
```

### Solution 3: Use Environment Variables (Alternative)

If the file still doesn't work, use environment variables:

```bash
export CLOJARS_USERNAME=alishsan
export CLOJARS_PASSWORD=CLOJARS_YOUR_TOKEN_HERE
lein deploy clojars
```

### Solution 4: Verify Token is Valid

Make sure your Clojars deploy token is:
- Still valid (not expired)
- Has deploy permissions
- Copied correctly (no extra spaces)

### Solution 5: Test Credentials File

```bash
# Test if file can be read as Clojure
clojure -e "(load-file \"~/.lein/credentials.clj\")"
```

If this fails, there's a syntax error in the file.

### Solution 6: Check Leiningen Version

```bash
lein version
```

Older versions might have different credential handling. Update if needed:
```bash
lein upgrade
```

## Current Status

Your credentials file is at: `~/.lein/credentials.clj`

If Leiningen still prompts, the environment variable approach (Solution 3) is the most reliable alternative.
