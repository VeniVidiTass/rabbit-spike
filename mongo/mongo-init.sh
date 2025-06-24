#!/usr/bin/env bash
set -e

# init RS if needed
mongosh --host appointments-db --quiet --eval '
  try { rs.initiate({_id:"rs0",members:[{_id:0,host:"appointments-db:27017"}]}); }
  catch(_) {}
'

# wait for PRIMARY
until [ "$(mongosh --host appointments-db --quiet --eval 'rs.isMaster().ismaster')" = "true" ]; do
  sleep 1
done

# seed
mongosh --host appointments-db gestmed_appointments_db /init/02-init-appointments.js