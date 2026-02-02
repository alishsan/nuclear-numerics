# Quick Guide: Publish to Clojars

## Step 1: Get Clojars Credentials

1. Go to https://clojars.org and sign up/login
2. Go to your profile page
3. Generate a deploy token
4. Copy the token

## Step 2: Set Up Credentials

Create `~/.lein/credentials.clj`:

```clojure
{#"clojars" {:username "YOUR_CLOJARS_USERNAME"
             :password "YOUR_DEPLOY_TOKEN"}}
```

**Important**: Replace:
- `YOUR_CLOJARS_USERNAME` with your Clojars username
- `YOUR_DEPLOY_TOKEN` with the token from step 1

## Step 3: Publish

```bash
cd /Users/a.sanetullaev/Development/clojure/nuclear-numerics
lein deploy clojars
```

## Step 4: Verify

After a few minutes, check:
https://clojars.org/alishsan/nuclear-numerics

## Step 5: Update esym

Once published, update `esym/project.clj`:

```clojure
:profiles {:dev {:dependencies [[alishsan/nuclear-numerics "0.1.0-SNAPSHOT" 
                                  :local/root "../nuclear-numerics"]]}
           :default {:dependencies [[alishsan/nuclear-numerics "0.1.0-SNAPSHOT"]]}}
```

The `:default` profile will now download from Clojars automatically!

## Troubleshooting

**"Authentication failed"**: 
- Double-check your credentials file
- Make sure you're using a deploy token, not your password

**"Artifact already exists"**:
- SNAPSHOT versions can be overwritten (just redeploy)
- Release versions need a new version number
