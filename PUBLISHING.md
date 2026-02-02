# Publishing nuclear-numerics to Clojars

## Prerequisites

1. **Clojars Account**: Sign up at https://clojars.org
2. **Leiningen**: Make sure you have Leiningen 2.x installed
3. **Credentials**: Set up your Clojars credentials

## Setting Up Clojars Credentials

### Option 1: Using ~/.lein/credentials.clj (Recommended)

Create `~/.lein/credentials.clj`:

```clojure
{#"clojars" {:username "YOUR_CLOJARS_USERNAME"
             :password "YOUR_CLOJARS_TOKEN"}}
```

To get your token:
1. Log in to https://clojars.org
2. Go to your profile
3. Generate a deploy token
4. Use that token as the password

### Option 2: Environment Variables

```bash
export CLOJARS_USERNAME=your-username
export CLOJARS_PASSWORD=your-token
```

### Option 3: GPG Signing (Optional)

For signed releases:
```bash
gpg --gen-key  # Generate a GPG key
gpg --list-keys  # List your keys
```

Then update `project.clj`:
```clojure
:deploy-repositories [["clojars" {:sign-releases true}]]
```

## Publishing Steps

### 1. Update Version (if needed)

Edit `project.clj` to set the version:
```clojure
(defproject alishsan/nuclear-numerics "0.1.0"  ; Remove -SNAPSHOT for release
```

Or keep SNAPSHOT for development releases:
```clojure
(defproject alishsan/nuclear-numerics "0.1.0-SNAPSHOT"
```

### 2. Verify Project

```bash
cd nuclear-numerics
lein check
lein test  # If you have tests
lein jar   # Build the JAR
```

### 3. Deploy to Clojars

```bash
lein deploy clojars
```

This will:
- Build the JAR
- Upload to Clojars
- Make it available for others to use

### 4. Verify Publication

Check https://clojars.org/alishsan/nuclear-numerics

The library should appear within a few minutes.

## Using the Published Library

Once published, users can add it to their `project.clj`:

```clojure
:dependencies [[alishsan/nuclear-numerics "0.1.0"]]
```

Then run:
```bash
lein deps
```

## Versioning Guidelines

- **SNAPSHOT versions** (e.g., "0.1.0-SNAPSHOT"): Development releases, can be overwritten
- **Release versions** (e.g., "0.1.0"): Stable releases, immutable once published

For development:
- Use SNAPSHOT versions
- Can redeploy same version
- Good for testing

For production:
- Use release versions
- Follow semantic versioning (MAJOR.MINOR.PATCH)
- Once published, cannot be changed

## Troubleshooting

### "Authentication failed"
- Check your credentials in `~/.lein/credentials.clj`
- Verify your Clojars username and token
- Make sure the token has deploy permissions

### "Artifact already exists"
- For SNAPSHOT: You can overwrite
- For releases: You must increment the version

### "GPG signing failed"
- Make sure you have a GPG key
- Or set `:sign-releases false` in `project.clj`

## Next Steps After Publishing

1. Update esym's `project.clj` to use the published version
2. Update documentation with Clojars badge
3. Add installation instructions to README
