// MongoDB initialization script
db = db.getSiblingDB('routemaster');

// Create collections
db.createCollection('parcels');
db.createCollection('route_logs');
db.createCollection('users');

// Create geospatial index on parcels collection
db.parcels.createIndex({ "currentLocation": "2dsphere" });
db.parcels.createIndex({ "trackingNumber": 1 }, { unique: true });
db.parcels.createIndex({ "status": 1 });
db.parcels.createIndex({ "assignedDriverId": 1 });

// Create indexes on route_logs
db.route_logs.createIndex({ "date": 1 });
db.route_logs.createIndex({ "driverId": 1 });

// Create indexes on users
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 });

// Create default admin user
db.users.insertOne({
    username: "admin",
    password: "$2a$10$XQjZ5K5K5K5K5K5K5K5K5uYvGqXqXqXqXqXqXqXqXqXqXqXqXqXqX", // BCrypt hash of "admin123"
    email: "admin@routemaster.com",
    fullName: "System Administrator",
    phone: "+1234567890",
    roles: ["ROLE_ADMIN", "ROLE_VIEWER"],
    enabled: true
});

// Create sample driver users
for (let i = 1; i <= 20; i++) {
    db.users.insertOne({
        username: `driver${i}`,
        password: "$2a$10$XQjZ5K5K5K5K5K5K5K5K5uYvGqXqXqXqXqXqXqXqXqXqXqXqXqXqX",
        email: `driver${i}@routemaster.com`,
        fullName: `Driver ${i}`,
        phone: `+123456789${i}`,
        roles: ["ROLE_DRIVER", "ROLE_VIEWER"],
        enabled: true
    });
}

print('MongoDB initialization completed successfully');
