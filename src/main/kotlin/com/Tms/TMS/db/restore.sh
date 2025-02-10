#!/bin/bash
set -e

if [ -s /backup/backup.dump ]; then
  echo "Restoring the PostgreSQL database from backup.dump..."
  pg_restore -U postgres -d postgres /backup/backup.dump
  echo "Database restoration complete!"
else
  echo "Error: backup.dump is missing or empty!"
  exit 1
fi

exec "$@"
