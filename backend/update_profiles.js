// Script to fetch users and update their profile info
const sequelize = require('./src/config/database');
const User = require('./src/models/User');

async function updateUserProfiles() {
    try {
        await sequelize.authenticate();
        console.log('Connected to database');

        // Update the existing user with profile info
        const result = await User.update(
            {
                phone_number: '(555) 019-2834',
                hospital_affiliation: "St. Mary's Teaching Hospital",
                license_id: 'PATH-99420'
            },
            {
                where: { role: 'PATHOLOGIST' },
                returning: true
            }
        );

        console.log(`Updated ${result[0]} pathologists with profile data`);
        
        const users = await User.findAll({ attributes: ['id', 'name', 'email', 'role', 'phone_number', 'hospital_affiliation', 'license_id'] });
        console.log('Current users:', JSON.stringify(users, null, 2));

        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

updateUserProfiles();
