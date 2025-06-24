// mongo/init-appointments.js

// Switch to the appointments database
// (unconditionally select the target DB so we don't stay in 'test')
db = db.getSiblingDB('gestmed_appointments_db');

// 1️⃣ Create collections with JSON Schema validation
// Services collection
if (!db.getCollectionInfos({ name: 'services' }).length) {
    db.createCollection('services', {
        validator: {
            $jsonSchema: {
                bsonType: 'object',
                required: ['name', 'doctor_id'],
                properties: {
                    name: { bsonType: 'string', description: 'must be a string and is required' },
                    description: { bsonType: 'string', description: 'must be a string' },
                    duration_minutes: { bsonType: 'int', description: 'must be an integer' },
                    price: { bsonType: 'double', description: 'must be a double' },
                    doctor_id: { bsonType: 'int', description: 'must be an integer and is required' },
                    is_active: { bsonType: 'bool', description: 'must be a boolean' },
                    is_external_bookable: { bsonType: 'bool', description: 'must be a boolean' },
                    created_at: { bsonType: 'date', description: 'must be a date' },
                    updated_at: { bsonType: 'date', description: 'must be a date' }
                }
            }
        },
        validationLevel: 'strict',
        validationAction: 'warn'
    });

    // Seed services if empty (slimmed down to 2 examples)
    if (db.services.countDocuments() === 0) {
        db.services.insertMany([
            {
                name: 'Visita Cardiologica',
                description: 'Visita specialistica cardiologica di base',
                duration_minutes: NumberInt(30),
                price: 80.0,
                doctor_id: NumberInt(1),
                is_active: true,
                is_external_bookable: true,
                created_at: new Date(),
                updated_at: new Date()
            },
            {
                name: 'Mappatura Nei',
                description: "Mappatura acustica dell'orecchio",
                duration_minutes: NumberInt(50),
                price: 100.0,
                doctor_id: NumberInt(4),
                is_active: true,
                is_external_bookable: true,
                created_at: new Date(),
                updated_at: new Date()
            }
        ]);
    }
}

// Appointments collection
if (!db.getCollectionInfos({ name: 'appointments' }).length) {
    db.createCollection('appointments', {
        validator: {
            $jsonSchema: {
                bsonType: 'object',
                required: ['patient_full_name', 'doctor_id', 'service_id', 'code', 'appointment_date'],
                properties: {
                    patient_id: { bsonType: ['int', 'null'], description: 'must be an integer or null' },
                    patient_full_name: { bsonType: 'string', description: 'must be a string and is required' },
                    patient_email: { bsonType: 'string', description: 'must be a string' },
                    patient_codice_fiscale: { bsonType: ['string', 'null'], description: 'must be a string or null' },
                    patient_phone: { bsonType: 'string', description: 'must be a string' },
                    doctor_id: { bsonType: 'int', description: 'must be an integer and is required' },
                    service_id: { bsonType: 'objectId', description: 'must be an ObjectId and is required' },
                    code: { bsonType: 'string', description: 'must be a string and is required' },
                    appointment_date: { bsonType: 'date', description: 'must be a date and is required' },
                    status: { bsonType: 'string', description: 'must be a string' },
                    notes: { bsonType: 'string', description: 'must be a string' },
                    created_at: { bsonType: 'date', description: 'must be a date' },
                    updated_at: { bsonType: 'date', description: 'must be a date' }
                }
            }
        },
        validationLevel: 'strict',
        validationAction: 'warn'
    });
}

// 2️⃣ Create indexes
// Services indexes
db.services.createIndex({ doctor_id: 1 });
db.services.createIndex({ is_active: 1 });
// Appointments indexes
db.appointments.createIndex({ appointment_date: 1 });
db.appointments.createIndex({ patient_id: 1 });
db.appointments.createIndex({ doctor_id: 1 });
db.appointments.createIndex({ service_id: 1 });
db.appointments.createIndex({ code: 1 }, { unique: true });

// 3️⃣ Helper to map service names to _id
const getId = (name, doctor_id) => db.services.findOne({ name, doctor_id })._id;

// 4️⃣ Insert sample appointments (slimmed down to 2 examples)
db.appointments.insertMany([
    {
        patient_id: 1,
        patient_full_name: 'Mario Rossi',
        patient_email: 'mario.rossi@email.com',
        patient_codice_fiscale: 'RSSMRA85M01H501Z',
        patient_phone: '+39 333 1234567',
        doctor_id: 1,
        service_id: getId('Visita Cardiologica', 1),
        code: 'AHUSX001',
        appointment_date: new Date('2025-06-15T10:00:00Z'),
        status: 'in_progress',
        notes: 'Controllo cardiologico di routine',
        created_at: new Date(),
        updated_at: new Date()
    },
    {
        patient_id: null,
        patient_full_name: 'Sofia Ferrari',
        patient_email: 'sofia.ferrari@email.com',
        patient_codice_fiscale: 'FRRSFR95E15H501A',
        patient_phone: '+39 338 1122334',
        doctor_id: 2,
        service_id: getId('Mappatura Nei', 4),
        code: '',
        appointment_date: new Date('2025-06-21T09:00:00Z'),
        status: 'scheduled',
        notes: 'Controllo crescita bambino',
        created_at: new Date(),
        updated_at: new Date()
    }
]);