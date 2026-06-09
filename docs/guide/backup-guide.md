# Backup Guide

## Test environment

During development, it's required to specify the test users who can get access the google drive API
in the development build.

There is a max of 100 allowed test users.

To add test users go to this link:

https://console.cloud.google.com/auth/audience?project=drink-water-app-869b5

## Usage

The backup allow to store your drink records in a cloud storage provider. Handling multiple devices
is also supported, but it requires the user to understand how the backup system works to avoid
conflicts and data lost.

# Synchronization

When you connect the first time to a provider a fist sync is performed:

1. No backup file exists

   You local data is uploaded to the provider automatically and sync finish with success.

2. Backup file exists

    - If the app determines the backup file contains the same data as your local data, the sync
      continues automatically and finish with success.

    - If the app determines the backup file has conflicts with your local data, user has to choose
      the next step:
        1. Merge → Combine the backup and your local data.
        2. Use backup → Replace your existing local data and use the backup as it is.
        3. Use local data → Replace the existing backup with your local data.

This last user options are only displayed when connecting to a cloud provider. If your intention is
to connect and start from scratch, you have to disconnect any existing connection and reconnect to
decide what to do with your backup.

NOTE: Any existent connection from other device can still merge existing data to your backup, as
user you are in charge to administrate how to back up your data between multiple devices without
conflicts.

can you investigate in the project if the work manager job is setup? there should be a feature to
run backup sync periodically , we should set the period in the settings screen by defining an
interval for the backups as well as a dedicated time if user decides so, this work should perform
the sync and merge data. only informing about issues in the ui settings when something needs help
from user AND sending notification when user help is needed (work manager has merge issues or any
other issue during synchronization)