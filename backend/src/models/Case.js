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
    mrn: {
        type: DataTypes.STRING,
        allowNull: true,  // Allow null for existing records, will be auto-generated on create
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
    validation_status: {
        type: DataTypes.STRING,
        defaultValue: 'pending', // 'pending', 'validated', 'signed_off'
        allowNull: false,
    },
    pathologist_report: {
        type: DataTypes.TEXT,
        allowNull: true,
    },
    validated_at: {
        type: DataTypes.DATE,
        allowNull: true,
    },
    validated_by_id: {
        type: DataTypes.INTEGER,
        allowNull: true,
    },
    signed_off_at: {
        type: DataTypes.DATE,
        allowNull: true,
    },
    // Foreign keys are handled in associations
}, {
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: false,
});

// Hook to auto-generate MRN
Case.beforeCreate(async (caseInstance) => {
    if (!caseInstance.mrn) {
        // Find the highest existing MRN and increment
        const lastCase = await Case.findOne({
            order: [['id', 'DESC']],
            raw: true,
        });
        
        let mrnNumber = 2828331; // Starting MRN
        if (lastCase && lastCase.mrn) {
            const lastMrn = parseInt(lastCase.mrn, 10);
            mrnNumber = lastMrn + 1;
        }
        caseInstance.mrn = mrnNumber.toString();
    }
});

module.exports = Case;
