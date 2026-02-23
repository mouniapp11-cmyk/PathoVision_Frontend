const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Case = sequelize.define('Case', {
    id: {
        type: DataTypes.UUID,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true,
    },
    title: {
        type: DataTypes.STRING,
        allowNull: false,
    },
    image_url: {
        type: DataTypes.STRING,
        allowNull: false,
    },
    ai_prediction: {
        type: DataTypes.STRING, // 'Benign' or 'Malignant'
        allowNull: true,
    },
    confidence_score: {
        type: DataTypes.FLOAT,
        allowNull: true,
    },
    doctor_notes: {
        type: DataTypes.TEXT,
        allowNull: true,
    },
    // Foreign keys are handled in associations
}, {
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: false,
});

module.exports = Case;
