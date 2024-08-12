#!/bin/bash

# Change to the directory of the script
cd "$HOME/opentelemetry-plugin-dm" || { echo "Failed to navigate to repository directory"; exit 1; }

# Run the commands sequentially
brew services stop jenkins-lts
mvn package -Dmaven.test.skip=true
cp -v target/harnessmigration.hpi ~/.jenkins/plugins
brew services start jenkins-lts

# Variables
HPI_FILE_PATH="$HOME/.jenkins/plugins/harnessmigration.hpi"
REPO_DIR="$HOME/Plugin_Repo"
BRANCH_NAME="master"
COMMIT_MESSAGE="Update harnessmigration.hpi file"

# Change to the repository directory
cd "$REPO_DIR" || { echo "Failed to navigate to repository directory"; exit 1; }

# Copy the .hpi file to the repository
cp -v "$HPI_FILE_PATH" "$REPO_DIR" || { echo "Failed to copy .hpi file"; exit 1; }

# Add the .hpi file to the Git index
git add "$(basename "$HPI_FILE_PATH")" || { echo "Failed to add .hpi file to git"; exit 1; }

# Commit the changes with a message
git commit -m "$COMMIT_MESSAGE" || { echo "Failed to commit changes"; exit 1; }

# Push the changes to the remote repository
git push origin "$BRANCH_NAME" || { echo "Failed to push changes to remote repository"; exit 1; }

echo "Successfully copied, committed, and pushed the .hpi file to the repository."

cd "$HOME/opentelemetry-plugin-dm" || { echo "Failed to navigate to repository directory"; exit 1; }

source .env

#echo "Your API Key is: $X_API_KEY"

curl -i -X POST \
  "https://app.harness.io/v1/orgs/$ORGANIZATION_IDENTIFIER/projects/$PROJECT_IDENTIFIER/pipelines/$PIPELINE/execute?module=string&use_fqn_if_error_response=false&notify_only_user=false&notes=string&branch_name=string&connector_ref=string&repo_name=string" \
  -H "Content-Type: application/json" \
  -H "Harness-Account: $ACCOUNT_IDENTIFIER" \
  -H "x-api-key: $X_API_KEY" \
