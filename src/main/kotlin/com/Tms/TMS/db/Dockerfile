FROM postgres:16

ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=kavipam27
ENV POSTGRES_DB=postgres

# Copy the backup file into the container
COPY backup.dump /backup/backup.dump

# Restore the database when the container starts
CMD ["postgres"]

# Use an entrypoint script to restore the database
COPY restore.sh /docker-entrypoint-initdb.d/restore.sh
RUN chmod +x /docker-entrypoint-initdb.d/restore.sh
