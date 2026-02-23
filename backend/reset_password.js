const bcrypt = require('bcrypt');
const sequelize = require('./src/config/database');
const User = require('./src/models/User');

async function resetPassword() {
    try {
        await sequelize.authenticate();
        console.log('Connected to database');

        // Hash the new password
        const hashedPassword = await bcrypt.hash('password123', 10);
        
        // Update the user
        const [updatedCount] = await User.update(
            { password_hash: hashedPassword },
            { where: { email: 'hemanthbezawada7@gmail.com' } }
        );

        console.log(`Updated ${updatedCount} user(s) with new password`);

        // Verify the user exists
        const user = await User.findOne({ where: { email: 'hemanthbezawada7@gmail.com' } });
        if (user) {
            console.log('User found:', user.name);
            console.log('Email:', user.email);
            console.log('Testing password...');
            const isMatch = await bcrypt.compare('password123', user.password_hash);
            console.log('Password match:', isMatch);
        } else {
            console.log('User not found!');
        }

        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

resetPassword();
