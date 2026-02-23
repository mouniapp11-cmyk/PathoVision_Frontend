const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const User = sequelize.define('User', {
    id: {
        type: DataTypes.UUID,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true,
    },
    name: {
        type: DataTypes.STRING,
        allowNull: false,
    },
    email: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true,
        validate: {
            isEmail: true,
        },
    },
    password_hash: {
        type: DataTypes.STRING,
        allowNull: false,
    },
    role: {
        type: DataTypes.ENUM('PATHOLOGIST', 'PATIENT', 'STUDENT'),
        allowNull: false,
    },
    phone_number: {
        type: DataTypes.STRING,
        allowNull: true,
    },
    hospital_affiliation: {
        type: DataTypes.STRING,
        allowNull: true,
    },
    license_id: {
        type: DataTypes.STRING,
        allowNull: true,
    },
    profile_picture: {
        type: DataTypes.STRING,
        allowNull: true,
    },
}, {
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: false,
});

module.exports = User;
